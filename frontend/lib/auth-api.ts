import { apiRequest } from "./api";
import { saveToken, saveRole } from "./auth";

export interface LoginRequest {
  email: string;
  password: string;
}

export type UserRole =
  | "BUYER"
  | "REAL_ESTATE_AGENT"
  | "LEGAL_REVIEWER"
  | "FINANCIAL_INSTITUTION"
  | "ADMINISTRATOR";

export type RegistrableRole =
  | "BUYER"
  | "REAL_ESTATE_AGENT"
  | "LEGAL_REVIEWER"
  | "FINANCIAL_INSTITUTION";

export interface LoginResponse {
  id: number;
  fullName: string;
  email: string;
  role: UserRole;
  token: string;
}

export interface RegisterRequest {
  fullName: string;
  email: string;
  password: string;
  role: RegistrableRole;
}

export interface RegisterResponse {
  id: number;
  fullName: string;
  email: string;
  role: UserRole;
}

export async function login(
  credentials: LoginRequest
): Promise<LoginResponse> {
  const response = await apiRequest<LoginResponse>("/api/auth/login", {
    method: "POST",
    body: JSON.stringify(credentials),
  });

  saveToken(response.token);
  if (response.role) {
    saveRole(response.role);
  }

  return response;
}

export async function register(
  data: RegisterRequest
): Promise<RegisterResponse> {
  return await apiRequest<RegisterResponse>("/api/auth/register", {
    method: "POST",
    body: JSON.stringify(data),
  });
}