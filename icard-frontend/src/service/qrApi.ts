import axiosInstance from "./axiosInstance";
import type { QRCodeResponse, QRVerifyRequest } from "../types/qr";
import type { AttendanceResponse } from "../types/attendance";

export const generateQrCode = async (): Promise<QRCodeResponse> => {
  const response = await axiosInstance.post<QRCodeResponse>("/qr/generate");
  return response.data;
};

export const verifyQrCode = async (data: QRVerifyRequest): Promise<AttendanceResponse> => {
  const response = await axiosInstance.post<AttendanceResponse>("/qr/verify", data);
  return response.data;
};