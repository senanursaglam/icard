import axiosInstance from "./axiosInstance";
import type { UserResponse } from "../types/auth";
import type {
  AdminUserCreateRequest,
  AdminUserUpdateRequest,
  DepartmentRequest,
  DepartmentResponse,
  WorkScheduleRequest,
  WorkScheduleResponse,
} from "../types/admin";
import type { WeeklyChartResponse } from "../types/attendance";
import type { AttendanceFilterRequest, AttendanceResponse, PageResponse } from "../types/attendance";
import type { AdminUserFilterRequest } from "../types/admin";

export const adminGetAllUsers = async (
  request: AdminUserFilterRequest
): Promise<PageResponse<UserResponse>> => {
  const response = await axiosInstance.post<PageResponse<UserResponse>>("/admin/users/filter", request);
  return response.data;
};
export const adminCreateUser = async (data: AdminUserCreateRequest): Promise<UserResponse> => {
  const response = await axiosInstance.post<UserResponse>("/admin/users", data);
  return response.data;
};

export const adminUpdateUser = async (id: number, data: AdminUserUpdateRequest): Promise<UserResponse> => {
  const response = await axiosInstance.put<UserResponse>(`/admin/users/${id}`, data);
  return response.data;
};

export const adminDeactivateUser = async (id: number): Promise<void> => {
  await axiosInstance.delete(`/admin/users/${id}`);
};

export const getDepartments = async (): Promise<DepartmentResponse[]> => {
  const response = await axiosInstance.get<DepartmentResponse[]>("/departments");
  return response.data;
};

export const adminCreateDepartment = async (data: DepartmentRequest): Promise<DepartmentResponse> => {
  const response = await axiosInstance.post<DepartmentResponse>("/admin/departments", data);
  return response.data;
};

export const adminUpdateDepartment = async (id: number, data: DepartmentRequest): Promise<DepartmentResponse> => {
  const response = await axiosInstance.put<DepartmentResponse>(`/admin/departments/${id}`, data);
  return response.data;
};

export const getWorkSchedules = async (): Promise<WorkScheduleResponse[]> => {
  const response = await axiosInstance.get<WorkScheduleResponse[]>("/work-schedules");
  return response.data;
};

export const adminCreateWorkSchedule = async (data: WorkScheduleRequest): Promise<WorkScheduleResponse> => {
  const response = await axiosInstance.post<WorkScheduleResponse>("/admin/work-schedules", data);
  return response.data;
};

export const adminUpdateWorkSchedule = async (id: number, data: WorkScheduleRequest): Promise<WorkScheduleResponse> => {
  const response = await axiosInstance.put<WorkScheduleResponse>(`/admin/work-schedules/${id}`, data);
  return response.data;
};

export const adminFilterAttendance = async (
  request: AttendanceFilterRequest
): Promise<PageResponse<AttendanceResponse>> => {
  const response = await axiosInstance.post<PageResponse<AttendanceResponse>>(
    "/admin/attendance/filter",
    request
  );
  return response.data;
};