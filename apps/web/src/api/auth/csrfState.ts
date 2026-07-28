export type CsrfToken = {
    token: string;
    headerName: string;
};

let storedCsrfToken: CsrfToken | null = null;

export function getCsrfToken(): CsrfToken | null {
    return storedCsrfToken;
}

export function saveCsrfToken(token: CsrfToken): void {
    storedCsrfToken = token;
}

export function clearCsrfToken(): void {
    storedCsrfToken = null;
}
