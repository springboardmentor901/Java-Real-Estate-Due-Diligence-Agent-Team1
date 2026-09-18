const TOKEN_KEY = "due_diligence_token";
const ROLE_KEY = "due_diligence_role";

export function saveToken(token: string) {
  if (typeof window !== "undefined") {
    localStorage.setItem(TOKEN_KEY, token);
  }
}

export function getToken(): string | null {
  if (typeof window !== "undefined") {
    return localStorage.getItem(TOKEN_KEY);
  }

  return null;
}

export function saveRole(role: string) {
  if (typeof window !== "undefined") {
    localStorage.setItem(ROLE_KEY, role);
  }
}

export function getRole(): string | null {
  if (typeof window !== "undefined") {
    return localStorage.getItem(ROLE_KEY);
  }

  return null;
}

export function removeToken() {
  if (typeof window !== "undefined") {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(ROLE_KEY);
  }
}

export function isLoggedIn(): boolean {
  return getToken() !== null;
}