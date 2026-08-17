const apiBaseUrl = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

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
  return postJson<AuthResponse>("/api/auth/login", payload);
}

export async function registerUser(payload: RegisterPayload): Promise<unknown> {
  return postJson<unknown>("/api/auth/register", payload);
}

async function postJson<T>(path: string, payload: unknown): Promise<T> {
  const response = await fetch(`${apiBaseUrl}${path}`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(payload),
  });

  if (!response.ok) {
    throw new Error(await getErrorMessage(response));
  }

  return response.json() as Promise<T>;
}

async function getErrorMessage(response: Response) {
  try {
    const body = await response.json();
    return body.message ?? body.error ?? "Request failed.";
  } catch {
    return "Request failed.";
  }
}
