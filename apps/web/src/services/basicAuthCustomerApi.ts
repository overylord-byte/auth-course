/**
 * Calls the course API through the Vite dev proxy (same origin) so the browser
 * does not require CORS changes on the Spring Boot app.
 */
const API_ROOT = import.meta.env.VITE_API_URL ?? "";

export type CustomersPageResponse = {
  totalElements?: number;
};

function toBase64Utf8(value: string): string {
  const bytes = new TextEncoder().encode(value);
  let binary = "";
  bytes.forEach((b) => {
    binary += String.fromCharCode(b);
  });
  return btoa(binary);
}

export function buildBasicAuthorizationHeader(
  username: string,
  password: string,
): string {
  return `Basic ${toBase64Utf8(`${username}:${password}`)}`;
}

export type FetchCustomersResult =
  | { ok: true; totalElements: number }
  | { ok: false; status: number };

export async function fetchCustomersPage(
  username: string,
  password: string,
): Promise<FetchCustomersResult> {
  const headers: HeadersInit = {
    Authorization: buildBasicAuthorizationHeader(username, password),
    Accept: "application/json",
  };

  const res = await fetch(`${API_ROOT}/api/v1/customer`, { headers });

  if (res.status === 401) {
    return { ok: false, status: 401 };
  }

  if (!res.ok) {
    return { ok: false, status: res.status };
  }

  const body = (await res.json()) as CustomersPageResponse;
  return { ok: true, totalElements: body.totalElements ?? 0 };
}
