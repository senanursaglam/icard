import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { changePassword } from "../service/authApi";
import { message } from "antd";
import "./Auth.css";


const ChangePasswordPage = () => {
  const navigate = useNavigate();

  const [newPassword, setNewPassword] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    setLoading(true);

    try {
      await changePassword(newPassword);

      message.success("Şifreniz başarıyla değiştirildi.");

      const storedUser = localStorage.getItem("user");

      if (storedUser) {
        const user = JSON.parse(storedUser);
        user.temporaryPassword = false;
        localStorage.setItem("user", JSON.stringify(user));

        if (user.role === "MANAGER") {
          navigate("/manager/team");
        } else if (user.role === "ADMIN") {
          navigate("/admin");
        } else {
          navigate("/dashboard");
        }
      }
    } catch (err: any) {
      message.error(
        err.response?.data?.message || "Şifre değiştirilemedi."
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <h2 className="auth-title">Yeni Şifre Oluştur</h2>

        <p className="auth-sub">
          İlk girişiniz olduğu için yeni bir şifre belirlemeniz gerekiyor.
        </p>

        <form onSubmit={handleSubmit}>
          <div className="field-group">
            <label className="field-label">
              Yeni Şifre
            </label>

            <input
              className="field-input"
              type="password"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              minLength={6}
              required
            />
          </div>

          <button
            type="submit"
            className="auth-submit"
            disabled={loading}
          >
            {loading ? "Kaydediliyor..." : "Şifreyi Değiştir"}
          </button>
        </form>
      </div>
    </div>
  );
};

export default ChangePasswordPage;