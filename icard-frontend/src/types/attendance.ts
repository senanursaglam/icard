export type AttendanceStatus = "ACTIVE" | "COMPLETED";

export interface AttendanceResponse {
  id: number;
  loginTime: string;
  logoutTime: string | null;
  workedMinutes: number | null;
  workedHours: number | null;
  status: AttendanceStatus;
}

export interface DashboardStatsResponse {
  weeklyWorkedHours: number;
  todayWorkedHours: number;
  weeklyCheckInCount: number;
  weeklyCheckOutCount: number;
  totalWorkedHours: number;
  weeklyLeaveDayCount: number;
}

export interface PageResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}
export interface DailyHoursResponse {
  day: string;
  hours: number;
  isLeave: boolean;
}

export type FilterOperator = "EQ" | "LIKE" | "IN" | "BETWEEN" | "DATE_BETWEEN";

export interface FilterCriteria {
  field: string;
  operator: FilterOperator;
  value?: string;
  values?: string[];
}

export interface AttendanceFilterRequest {
  page: number;
  size: number;
  sort?: string;
  filters: FilterCriteria[];
}
export interface WeeklyChartResponse {
  weekStart: string;
  weekEnd: string;
  days: DailyHoursResponse[];
}
export interface ActiveStatusResponse {
  active: boolean;
}