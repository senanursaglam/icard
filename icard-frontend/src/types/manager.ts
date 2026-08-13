export interface TeamOvertimeSummaryResponse {
  userFullName: string;
  totalOvertimeHours: number;
  lateCount: number;
}
import type { UserResponse } from "./auth";
import type { AttendanceResponse } from "./attendance";

export interface EmployeeDetailResponse {
  user: UserResponse;
  recentAttendance: AttendanceResponse[];
  totalOvertimeHours: number;
  lateCount: number;
  pendingLeaveCount: number;
}