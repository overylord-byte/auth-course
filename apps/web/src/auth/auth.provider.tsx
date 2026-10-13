import {useCallback, useEffect, useMemo, useRef, useState, type ReactNode} from "react";
import {checkSession, type SessionCheckResult} from "../api/auth/sessionState";
import {clearCsrfToken} from "../api/auth/csrfState";
import {AuthContext, type AuthState} from "./auth.context.ts";

export function AuthProvider({children}: {children: ReactNode}) {
    const [state, setState] = useState<AuthState>({status: "checking"});
    const [attempt, setAttempt] = useState(0);
    const version = useRef(0);
    const request = useRef<Promise<SessionCheckResult> | null>(null);

    const clearSession = useCallback(() => {
        version.current += 1;
        request.current = null;
        clearCsrfToken();
        setState({status: "anonymous"});
    }, []);

    const signIn = useCallback((username: string) => {
        version.current += 1;
        request.current = null;
        setState({status: "authenticated", username, version: version.current});
    }, []);

    const retry = useCallback(() => {
        version.current += 1;
        request.current = null;
        setState({status: "checking"});
        setAttempt(value => value + 1);
    }, []);

    useEffect(() => {
        let cancelled = false;
        const expectedVersion = version.current;
        // StrictMode subscribers share the same request; stale results cannot restore auth.
        request.current ??= checkSession();

        void request.current.then(result => {
            if (cancelled || version.current !== expectedVersion) return;
            if (result.ok) {
                signIn(result.username);
            } else if ("sessionLost" in result) {
                clearSession();
            } else {
                setState({status: "error"});
            }
        }).catch(() => {
            if (!cancelled && version.current === expectedVersion) {
                setState({status: "error"});
            }
        });

        return () => {
            cancelled = true;
        };
    }, [attempt, clearSession, signIn]);

    const value = useMemo(() => ({state, signIn, clearSession, retry}),
        [state, signIn, clearSession, retry]);

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
