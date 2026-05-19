import type { Customer } from "./customers";

const API_ROOT = import.meta.env.VITE_API_URL ?? "";

type LoginResponseBody = {
  authenticated?: boolean;
  username?: string;
};

type CustomersResponseBody = {
  content?: Customer[];
};

export type LoginWithSessionResult =
  | { ok: true; username: string }
  | { ok: false; status: number };

export async function loginWithSession(
  username: string,
  password: string,
): Promise<LoginWithSessionResult> {
  const response = await fetch(`${API_ROOT}/api/v1/auth/login`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    credentials: "include",
    body: JSON.stringify({ username, password }),
  });

  if (response.status === 401) {
    return { ok: false, status: 401 };
  }

  if (!response.ok) {
    return { ok: false, status: response.status };
  }

  const body = (await response.json()) as LoginResponseBody;
  return { ok: true, username: body.username ?? username };
}

export type FetchCustomersWithSessionResult =
  | { ok: true; customers: Customer[] }
  | { ok: false; status: number };

export async function fetchCustomersWithSession(): Promise<FetchCustomersWithSessionResult> {
  const response = await fetch(`${API_ROOT}/api/v1/customer`, {
    method: "GET",
    credentials: "include",
    headers: {
      Accept: "application/json",
    },
  });

  if (!response.ok) {
    return { ok: false, status: response.status };
  }

  const body = (await response.json()) as CustomersResponseBody;
  return { ok: true, customers: body.content ?? [] };
}

export type LogoutSessionResult = { ok: true } | { ok: false; status: number };

export async function logoutSession(): Promise<LogoutSessionResult> {
  const response = await fetch(`${API_ROOT}/api/v1/auth/logout`, {
    method: "POST",
    credentials: "include",
  });

  if (!response.ok) {
    return { ok: false, status: response.status };
  }

  return { ok: true };
}
