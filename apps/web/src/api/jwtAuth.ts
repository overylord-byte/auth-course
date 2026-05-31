import type { Customer } from "./customers";

const API_ROOT = import.meta.env.VITE_API_URL ?? "";

type TokenResponseBody = {
  accessToken?: string;
  tokenType?: string;
  expiresIn?: number;
};

type CustomersResponseBody = {
  content?: Customer[];
};

export type LoginWithJwtResult =
  | { ok: true; accessToken: string }
  | { ok: false; status: number };

export async function loginWithJwt(
  username: string,
  password: string,
): Promise<LoginWithJwtResult> {
  const response = await fetch(`${API_ROOT}/api/v1/auth/accessToken`, {
    method: "POST",
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ username, password }),
  });

  if (response.status === 401) {
    return { ok: false, status: 401 };
  }

  if (!response.ok) {
    return { ok: false, status: response.status };
  }

  const body = (await response.json()) as TokenResponseBody;

  if (!body.accessToken) {
    return { ok: false, status: response.status };
  }

  return { ok: true, accessToken: body.accessToken };
}

export type RefreshAccessTokenResult =
  | { ok: true; accessToken: string }
  | { ok: false; status: number };

export async function refreshAccessToken(): Promise<RefreshAccessTokenResult> {
  const response = await fetch(`${API_ROOT}/api/v1/auth/refresh`, {
    method: "POST",
    credentials: "include",
  });

  if (!response.ok) {
    return { ok: false, status: response.status };
  }

  const body = (await response.json()) as TokenResponseBody;

  if (!body.accessToken) {
    return { ok: false, status: response.status };
  }

  return { ok: true, accessToken: body.accessToken };
}

export type LogoutWithJwtResult = { ok: true } | { ok: false; status: number };

export async function logoutWithJwt(): Promise<LogoutWithJwtResult> {
  const response = await fetch(`${API_ROOT}/api/v1/auth/logout`, {
    method: "POST",
    credentials: "include",
  });

  if (!response.ok) {
    return { ok: false, status: response.status };
  }

  return { ok: true };
}

export type FetchCustomersWithJwtResult =
  | { ok: true; customers: Customer[] }
  | { ok: false; status: number };

export async function fetchCustomersWithJwt(
  accessToken: string,
): Promise<FetchCustomersWithJwtResult> {
  const response = await fetch(`${API_ROOT}/api/v1/customer`, {
    method: "GET",
    headers: {
      Accept: "application/json",
      Authorization: `Bearer ${accessToken}`,
    },
  });

  if (!response.ok) {
    return { ok: false, status: response.status };
  }

  const body = (await response.json()) as CustomersResponseBody;
  return { ok: true, customers: body.content ?? [] };
}
