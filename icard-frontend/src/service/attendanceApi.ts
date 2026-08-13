import axiosInstance from "./axiosInstance";
import type {
  AttendanceResponse,
  DashboardStatsResponse,
  PageResponse,
  AttendanceFilterRequest,
  WeeklyChartResponse,
  ActiveStatusResponse,
} from "../types/attendance";

export const getDashboardStats = async (): Promise<DashboardStatsResponse> => {
  const response = await axiosInstance.get<DashboardStatsResponse>("/dashboard/stats");
  return response.data;
};

export const getAttendanceHistory = async (
  page: number,
  size: number
): Promise<PageResponse<AttendanceResponse>> => {
  const response = await axiosInstance.get<PageResponse<AttendanceResponse>>("/attendance", {
    params: { page, size, sort: "loginTime,desc" },
  });
  return response.data;
};

export const getWeeklyChart = async (weekOffset: number = 0): Promise<WeeklyChartResponse> => {
  const response = await axiosInstance.get<WeeklyChartResponse>("/dashboard/weekly-chart", {
    params: { weekOffset },
  });
  return response.data;
};

export const checkOutAttendance = async (): Promise<AttendanceResponse> => {
  const response = await axiosInstance.post<AttendanceResponse>("/attendance/checkout");
  return response.data;
};

export const filterAttendance = async (
  request: AttendanceFilterRequest
): Promise<PageResponse<AttendanceResponse>> => {
  const response = await axiosInstance.post<PageResponse<AttendanceResponse>>(
    "/attendance/filter",
    request
  );
  return response.data;
};
export const getActiveStatus = async (): Promise<ActiveStatusResponse> => {
  const response = await axiosInstance.get<ActiveStatusResponse>("/attendance/active-status");
  return response.data;
};

export const getDepartmentWeeklyChart = async (
  departmentId: number,
  weekOffset: number = 0
): Promise<WeeklyChartResponse> => {
  const response = await axiosInstance.get<WeeklyChartResponse>(
    `/dashboard/department/${departmentId}/weekly-chart`,
    { params: { weekOffset } }
  );
  return response.data;
};