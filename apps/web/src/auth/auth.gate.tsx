import {Fragment, type ReactNode} from "react";
import {Navigate} from "react-router-dom";
import {useAuth} from "./auth.context.ts";

export function AuthGate({children}: {children: ReactNode}) {
    const {state, retry} = useAuth();

    if (state.status === "checking") {
        return (
            <main className="flex min-h-screen items-center justify-center">
                <p role="status">Checking session...</p>
            </main>
        );
    }
    if (state.status === "error") {
        return (
            <main className="flex min-h-screen flex-col items-center justify-center gap-4">
                <p role="alert">Unable to check your session. Please try again.</p>
                <button type="button" onClick={retry}
                        className="rounded-md bg-slate-800 px-4 py-2 text-white">Retry</button>
            </main>
        );
    }
    return children;
}

export function RequireAuth({children}: {children: ReactNode}) {
    const {state} = useAuth();
    if (state.status !== "authenticated") return <Navigate to="/" replace />;

    // A new session must mount fresh page state, including its data and pending requests.
    return <Fragment key={state.version}>{children}</Fragment>;
}

export function GuestOnly({children}: {children: ReactNode}) {
    const {state} = useAuth();
    return state.status === "authenticated" ? <Navigate to="/customers" replace /> : children;
}
