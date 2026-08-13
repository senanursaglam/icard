import type { FilterCriteria } from "./attendance";

export type RoleType = "EMPLOYEE" | "MANAGER" | "ADMIN";

export interface AdminUserCreateRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  role?: RoleType;
  departmentId?: number;
  managerId?: number;
}

export interface AdminUserUpdateRequest {
  firstName?: string;
  lastName?: string;
  role?: RoleType;
  departmentId?: number;
  managerId?: number;
}

export interface DepartmentRequest {
  name: string;
  managerId?: number;
}

export interface DepartmentResponse {
  id: number;
  name: string;
  managerId: number | null;
  managerFullName: string | null;
  createdDate: string;
}

export interface WorkScheduleRequest {
  departmentId?: number;
  startTime: string;
  endTime: string;
  toleranceMinutes: number;
}

export interface WorkScheduleResponse {
  id: number;
  departmentId: number | null;
  departmentName: string;
  startTime: string;
  endTime: string;
  toleranceMinutes: number;
}

export interface AdminUserFilterRequest {
  page: number;
  size: number;
  filters: FilterCriteria[];
}