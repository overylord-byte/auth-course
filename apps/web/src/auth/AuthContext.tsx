import { createContext, useContext, useMemo, useState, type ReactNode } from "react";

type AuthContextValue = {
  accessToken: string | null;
  setAccessToken: (token: string) => void;
  clearAccessToken: () => void;
};

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [accessToken, setAccessTokenState] = useState<string | null>(null);

  const value = useMemo(
    () => ({
      accessToken,
      setAccessToken: (token: string) => setAccessTokenState(token),
      clearAccessToken: () => setAccessTokenState(null),
    }),
    [accessToken],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within AuthProvider");
  }
  return context;
}
