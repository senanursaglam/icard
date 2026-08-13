export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface UserResponse {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  createdDate: string;
  role: string;
  departmentName: string | null;
  managerFullName: string | null;
  active: boolean;
}

export interface AuthResponse {
  token: string;
  user: UserResponse;
}