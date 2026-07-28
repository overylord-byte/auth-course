import {clearCsrfToken, getCsrfToken, saveCsrfToken, type CsrfToken} from "./csrfState";
import {apiFetch, readJsonBody} from "../http";

type LoginResponseBody = {
    authenticated?: boolean;
    username?: string;
};

type CsrfTokenResponse = {
    token: string;
    headerName: string;
    parameterName: string;
};

export type {CsrfToken};

export type LoginWithSessionResult =
    | { ok: true; username: string; csrf: CsrfToken }
    | { ok: false; sessionLost: true }
    | { ok: false; status: number };

export type EnsureCsrfTokenResult =
    | { ok: true; csrf: CsrfToken }
    | { ok: false; sessionLost: true }
    | { ok: false; status: number };

function isSessionLost(
    result: Extract<EnsureCsrfTokenResult, { ok: false }>,
): result is Extract<EnsureCsrfTokenResult, { ok: false; sessionLost: true }> {
    return "sessionLost" in result;
}

export async function ensureCsrfToken(): Promise<EnsureCsrfTokenResult> {
    const cached = getCsrfToken();
    if (cached) {
        return {ok: true, csrf: cached};
    }

    const response = await apiFetch("/api/v1/auth/csrf", {
        method: "GET",
        headers: {
            Accept: "application/json",
        },
    });

    if (response.status === 401) {
        return {ok: false, sessionLost: true};
    }

    if (!response.ok) {
        return {ok: false, status: response.status};
    }

    const body = await readJsonBody<CsrfTokenResponse>(response);
    const csrfToken: CsrfToken = {
        token: body.token,
        headerName: body.headerName,
    };
    saveCsrfToken(csrfToken);
    return {ok: true, csrf: csrfToken};
}

export async function loginWithSession(
    username: string,
    password: string,
): Promise<LoginWithSessionResult> {
    const response = await apiFetch("/api/v1/auth/login", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({username, password}),
    });

    if (response.status === 401) {
        return {ok: false, status: 401};
    }

    if (!response.ok) {
        return {ok: false, status: response.status};
    }

    const body = await readJsonBody<LoginResponseBody>(response);
    const csrfResult = await ensureCsrfToken();
    if (!csrfResult.ok) {
        if (isSessionLost(csrfResult)) {
            return {ok: false, sessionLost: true};
        }
        return {ok: false, status: csrfResult.status};
    }

    return {
        ok: true,
        username: body.username ?? username,
        csrf: csrfResult.csrf,
    };
}

export type LogoutSessionResult =
    | { ok: true }
    | { ok: false; sessionLost: true }
    | { ok: false; status: number };

export async function logoutSession(): Promise<LogoutSessionResult> {
    const csrfResult = await ensureCsrfToken();
    if (!csrfResult.ok) {
        if (isSessionLost(csrfResult)) {
            clearCsrfToken();
            return {ok: false, sessionLost: true};
        }
        return {ok: false, status: csrfResult.status};
    }

    try {
        const response = await apiFetch("/api/v1/auth/logout", {
            method: "POST",
            headers: {
                [csrfResult.csrf.headerName]: csrfResult.csrf.token,
            },
        });

        if (!response.ok) {
            return {ok: false, status: response.status};
        }

        return {ok: true};
    } finally {
        clearCsrfToken();
    }
}
