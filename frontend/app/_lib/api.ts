const apiBaseUrl =
  process.env.NEXT_PUBLIC_API_BASE_URL?.replace(/\/$/, "") ??
  "http://localhost:8080";

type ApiRequestOptions = RequestInit & {
  auth?: boolean;
};

export type Property = {
  id: number;
  address: string;
  latitude: number | null;
  longitude: number | null;
  propertyType: string | null;
  bedrooms: number | null;
  bathrooms: number | null;
  squareFeet: number | null;
  yearBuilt: number | null;
  createdAt: string | null;
};

export type AddressValidation = {
  valid: boolean;
  formattedAddress: string | null;
  latitude: number | null;
  longitude: number | null;
};

export async function apiFetch<T>(
  path: string,
  { auth = true, headers, ...init }: ApiRequestOptions = {},
): Promise<T> {
  const requestHeaders = new Headers(headers);

  if (init.body && !requestHeaders.has("Content-Type")) {
    requestHeaders.set("Content-Type", "application/json");
  }

  if (auth) {
    const token = getAuthToken();

    if (!token) {
      throw new Error("Please sign in to continue.");
    }

    requestHeaders.set("Authorization", `Bearer ${token}`);
  }

  const response = await fetch(`${apiBaseUrl}${path}`, {
    ...init,
    headers: requestHeaders,
  });

  if (!response.ok) {
    throw new Error(await getErrorMessage(response));
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return response.json() as Promise<T>;
}

export function getAuthToken() {
  if (typeof window === "undefined") {
    return null;
  }

  return localStorage.getItem("authToken");
}

async function getErrorMessage(response: Response) {
  try {
    const body = await response.json();
    return body.message ?? body.error ?? "Request failed.";
  } catch {
    return "Request failed.";
  }
}
