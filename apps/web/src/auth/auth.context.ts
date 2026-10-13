import {createContext, useContext} from "react";

export type AuthState =
    | {status: "checking"}
    | {status: "authenticated"; username: string; version: number}
    | {status: "anonymous"}
    | {status: "error"};

export type AuthContextValue = {
    state: AuthState;
    signIn: (username: string) => void;
    clearSession: () => void;
    retry: () => void;
};

export const AuthContext = createContext<AuthContextValue | null>(null);

export function useAuth(): AuthContextValue {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error("useAuth must be used within AuthProvider");
    }
    return context;
}
