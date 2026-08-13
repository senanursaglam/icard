import axiosInstance from "./axiosInstance";
import type { UserResponse } from "../types/auth";
import type { AttendanceFilterRequest, AttendanceResponse, PageResponse } from "../types/attendance";
import type { LeaveRequestResponse } from "../types/leave";
import type { TeamOvertimeSummaryResponse, EmployeeDetailResponse } from "../types/manager";

export const getTeam = async (): Promise<UserResponse[]> => {
  const response = await axiosInstance.get<UserResponse[]>("/manager/team");
  return response.data;
};

export const getTeamAttendance = async (
  request: AttendanceFilterRequest
): Promise<PageResponse<AttendanceResponse>> => {
  const response = await axiosInstance.post<PageResponse<AttendanceResponse>>(
    "/manager/team/attendance",
    request
  );
  return response.data;
};

export const getTeamLeaves = async (
  page: number,
  size: number,
  status?: string
): Promise<PageResponse<LeaveRequestResponse>> => {
  const response = await axiosInstance.get<PageResponse<LeaveRequestResponse>>("/manager/leaves", {
    params: { page, size, status },
  });
  return response.data;
};

export const approveLeave = async (id: number): Promise<LeaveRequestResponse> => {
  const response = await axiosInstance.put<LeaveRequestResponse>(`/manager/leaves/${id}/approve`);
  return response.data;
};

export const rejectLeave = async (id: number): Promise<LeaveRequestResponse> => {
  const response = await axiosInstance.put<LeaveRequestResponse>(`/manager/leaves/${id}/reject`);
  return response.data;
};

export const getTeamOvertimeSummary = async (): Promise<TeamOvertimeSummaryResponse[]> => {
  const response = await axiosInstance.get<TeamOvertimeSummaryResponse[]>("/manager/team/overtime-summary");
  return response.data;
};

export const getEmployeeDetail = async (employeeId: number): Promise<EmployeeDetailResponse> => {
  const response = await axiosInstance.get<EmployeeDetailResponse>(`/manager/team/${employeeId}`);
  return response.data;
};