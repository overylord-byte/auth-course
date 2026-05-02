const API_ROOT = import.meta.env.VITE_API_URL ?? "";

export type Customer = {
  id: string;
  name: string;
};

type CustomersResponse = {
  content?: Customer[];
};

function toBase64Utf8(value: string): string {
  const bytes = new TextEncoder().encode(value);
  let binary = "";
  bytes.forEach((byte) => {
    binary += String.fromCharCode(byte);
  });
  return btoa(binary);
}

export function buildBasicAuthorizationHeader(username: string, password: string): string {
  return `Basic ${toBase64Utf8(`${username}:${password}`)}`;
}

export type FetchCustomersWithBasicAuthResult =
  | { ok: true; customers: Customer[] }
  | { ok: false; status: number };

export async function fetchCustomersWithBasicAuth(
  username: string,
  password: string,
): Promise<FetchCustomersWithBasicAuthResult> {
  const response = await fetch(`${API_ROOT}/api/v1/customer`, {
    method: "GET",
    headers: {
      Authorization: buildBasicAuthorizationHeader(username, password),
      Accept: "application/json",
    },
  });

  if (!response.ok) {
    return { ok: false, status: response.status };
  }

  const body = (await response.json()) as CustomersResponse;
  return { ok: true, customers: body.content ?? [] };
}
