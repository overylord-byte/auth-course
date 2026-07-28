export const API_ROOT = import.meta.env.VITE_API_URL ?? "";

type ApiFetchInit = Omit<RequestInit, "credentials">;

export function apiUrl(path: string): string {
    return `${API_ROOT}${path}`;
}

export async function apiFetch(path: string, init: ApiFetchInit = {}): Promise<Response> {
    return fetch(apiUrl(path), {
        ...init,
        credentials: "include",
    });
}

export async function readJsonBody<T>(response: Response): Promise<T> {
    return (await response.json()) as T;
}
