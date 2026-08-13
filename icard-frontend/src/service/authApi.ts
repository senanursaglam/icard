import axiosInstance from "./axiosInstance";
import type {LoginRequest, AuthResponse } from "../types/auth";

export const loginUser = async (data: LoginRequest): Promise<AuthResponse> => {
  const response = await axiosInstance.post<AuthResponse>("/auth/login", data);
  return response.data;
};

export const changePassword = async (newPassword: string): Promise<void> => {
  await axiosInstance.post("/auth/change-password", {
    newPassword,
  });
};