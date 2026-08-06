import axiosInstance from "./axiosInstance";
import type { RegisterRequest, LoginRequest, AuthResponse, UserResponse } from "../types/auth";

export const registerUser = async (data: RegisterRequest): Promise<UserResponse> => {
  const response = await axiosInstance.post<UserResponse>("/auth/register", data);
  return response.data;
};

export const loginUser = async (data: LoginRequest): Promise<AuthResponse> => {
  const response = await axiosInstance.post<AuthResponse>("/auth/login", data);
  return response.data;
};