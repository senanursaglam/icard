import axiosInstance from "./axiosInstance";
import type { LeaveRequestCreateRequest, LeaveRequestResponse } from "../types/leave";

export const createLeaveRequest = async (data: LeaveRequestCreateRequest): Promise<LeaveRequestResponse> => {
  const response = await axiosInstance.post<LeaveRequestResponse>("/leaves", data);
  return response.data;
};

export const getMyLeaves = async (): Promise<LeaveRequestResponse[]> => {
  const response = await axiosInstance.get<LeaveRequestResponse[]>("/leaves/me");
  return response.data;
};