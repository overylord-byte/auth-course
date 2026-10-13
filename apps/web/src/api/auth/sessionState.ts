import {apiFetch, readJsonBody} from "../http";

export type SessionCheckResult =
    | {ok: true; username: string}
    | {ok: false; sessionLost: true}
    | {ok: false; status: number};

export async function checkSession(): Promise<SessionCheckResult> {
    const response = await apiFetch("/api/v1/auth/session", {
        headers: {Accept: "application/json"},
        cache: "no-store",
    });

    if (response.status === 401) {
        return {ok: false, sessionLost: true};
    }
    if (!response.ok) {
        return {ok: false, status: response.status};
    }

    const body = await readJsonBody<{authenticated: boolean; username: string}>(response);
    if (!body.authenticated || typeof body.username !== "string") {
        throw new Error("Invalid session response");
    }
    return {ok: true, username: body.username};
}
