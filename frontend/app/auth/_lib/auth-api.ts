import { apiFetch } from "@/app/_lib/api";

type LoginPayload = {
  email: string;
  password: string;
};

type RegisterPayload = LoginPayload & {
  fullName: string;
  role: string;
};

type AuthResponse = {
  id: number;
  fullName: string;
  email: string;
  role: string;
  token: string;
};

export async function loginUser(payload: LoginPayload): Promise<AuthResponse> {
  return apiFetch<AuthResponse>("/api/auth/login", {
    method: "POST",
    body: JSON.stringify(payload),
    auth: false,
  });
}

export async function registerUser(payload: RegisterPayload): Promise<unknown> {
  return apiFetch<unknown>("/api/auth/register", {
    method: "POST",
    body: JSON.stringify(payload),
    auth: false,
  });
}
