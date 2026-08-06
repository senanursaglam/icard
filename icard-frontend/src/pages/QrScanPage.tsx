import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Card, Button, message, Spin } from "antd";
import { Html5Qrcode } from "html5-qrcode";
import { generateQrCode, verifyQrCode } from "../service/qrApi";

const QR_ELEMENT_ID = "qr-reader";

let globalIsGenerating = false;

const QrScanPage = () => {
  const navigate = useNavigate();
  const scannerRef = useRef<Html5Qrcode | null>(null);
  const startPromiseRef = useRef<Promise<any> | null>(null);
  const cancelledRef = useRef(false);
  const countdownIntervalRef = useRef<number | null>(null);

  const [loading, setLoading] = useState(true);
  const [expired, setExpired] = useState(false);
  const [remainingSeconds, setRemainingSeconds] = useState<number | null>(null);

  useEffect(() => {
    cancelledRef.current = false;
    startFlow();
    return () => {
      cancelledRef.current = true;
      stopScanner();
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const startFlow = async () => {
    if (globalIsGenerating) return;
    globalIsGenerating = true;

    setLoading(true);
    setExpired(false);

    try {
      const qr = await generateQrCode();
      message.success("QR kodu mailine gönderildi.");

      setLoading(false);
      await new Promise((resolve) => setTimeout(resolve, 0));
      await startScanner();

      const expireTime = new Date(qr.expireDate).getTime();
      startCountdown(expireTime);
    } catch {
      if (!cancelledRef.current) {
        message.error("QR kodu üretilemedi.");
      }
    } finally {
      globalIsGenerating = false;
    }
  };

  const startCountdown = (expireTime: number) => {
    const tick = () => {
      const secondsLeft = Math.max(0, Math.floor((expireTime - Date.now()) / 1000));
      setRemainingSeconds(secondsLeft);

      if (secondsLeft <= 0) {
        if (countdownIntervalRef.current) {
          clearInterval(countdownIntervalRef.current);
          countdownIntervalRef.current = null;
        }
        setExpired(true);
        stopScanner();
      }
    };

    tick();
    countdownIntervalRef.current = window.setInterval(tick, 1000);
  };

  const startScanner = async () => {
    try {
      const scanner = new Html5Qrcode(QR_ELEMENT_ID);
      scannerRef.current = scanner;

      const startPromise = scanner.start(
        { facingMode: "environment" },
        { fps: 10, qrbox: 250 },
        async (decodedText) => {
          await handleScanSuccess(decodedText);
        },
        () => {
          // sessizce geç
        }
      );

      startPromiseRef.current = startPromise;
      await startPromise;
    } catch {
      if (!cancelledRef.current) {
        message.error("Kamera açılamadı.");
      }
    } finally {
      startPromiseRef.current = null;
    }
  };

  const stopScanner = async () => {
    if (countdownIntervalRef.current) {
      clearInterval(countdownIntervalRef.current);
      countdownIntervalRef.current = null;
    }

    if (startPromiseRef.current) {
      try {
        await startPromiseRef.current;
      } catch {
        // start zaten başarısız olmuşsa sorun değil
      }
    }

    if (scannerRef.current) {
      try {
        const state = scannerRef.current.getState();
        if (state === 2 /* SCANNING */) {
          await scannerRef.current.stop();
        }
        scannerRef.current.clear();
      } catch {
        // zaten durmuşsa hata verebilir, göz ardı ediyoruz
      }
      scannerRef.current = null;
    }

    const videoElement = document.querySelector(
      `#${QR_ELEMENT_ID} video`
    ) as HTMLVideoElement | null;
    if (videoElement?.srcObject) {
      const stream = videoElement.srcObject as MediaStream;
      stream.getTracks().forEach((track) => track.stop());
      videoElement.srcObject = null;
    }
  };

  const handleScanSuccess = async (decodedText: string) => {
    await stopScanner();
    try {
      await verifyQrCode({ code: decodedText });
      message.success("Giriş başarılı!");
      navigate("/dashboard");
    } catch (err: any) {
      message.error(err.response?.data?.message || "QR doğrulanamadı.");
      cancelledRef.current = false;
      startScanner();
    }
  };

  const handleResend = () => {
    cancelledRef.current = false;
    startFlow();
  };

  const handleCancel = async () => {
    cancelledRef.current = true;
    await stopScanner();
    navigate("/dashboard");
  };

  const formatCountdown = (totalSeconds: number) => {
    const minutes = Math.floor(totalSeconds / 60);
    const seconds = totalSeconds % 60;
    return `${minutes}:${seconds.toString().padStart(2, "0")}`;
  };

  return (
    <div style={{ maxWidth: 500, margin: "40px auto", textAlign: "center" }}>
      <Card>
        <h2>Karekodu Okut</h2>
        {!expired && <p>Mailinize gönderilen QR kodunu, kameraya gösterin.</p>}

        {loading && <Spin />}

        <div
          id={QR_ELEMENT_ID}
          style={{
            width: "100%",
            display: !expired ? "block" : "none",
          }}
        />

        {expired && (
          <div>
            <p>QR kodunun süresi doldu.</p>
            <Button type="primary" onClick={handleResend}>
              Yeni Kod Gönder
            </Button>
          </div>
        )}

        {remainingSeconds !== null && !expired && (
          <p style={{ marginTop: 12, color: "gray" }}>
            Kalan süre: {formatCountdown(remainingSeconds)}
          </p>
        )}

        <Button style={{ marginTop: 16 }} onClick={handleCancel}>
          Vazgeç
        </Button>
      </Card>
    </div>
  );
};

export default QrScanPage;