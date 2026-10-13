import { useCallback, useEffect, useRef, useState } from "react";
import { logoutSession } from "../api/auth/sessionAuth";
import { useAuth } from "../auth/auth.context.ts";
import {Customer, fetchCustomersWithSession} from "../api/customers/sessionCustomers";
import { CustomersTable } from "../components/CustomersTable";
import { LogoutButton } from "../components/LogoutButton";

export function CustomersPage() {
  const {clearSession} = useAuth();
  const customersRequest = useRef<ReturnType<typeof fetchCustomersWithSession> | null>(null);
  const [customers, setCustomers] = useState<Customer[] | null>(null);
  const [loading, setLoading] = useState(true);
  const [logoutLoading, setLogoutLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [logoutError, setLogoutError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;

    async function loadCustomers() {
      setLoading(true);
      setErrorMessage(null);

      try {
        customersRequest.current ??= fetchCustomersWithSession();
        const result = await customersRequest.current;

        if (cancelled) return;

        if (result.ok) {
          setCustomers(result.customers);
          return;
        }

        if (result.status === 401) {
          clearSession();
          return;
        }

        setErrorMessage("Failed to load customers. Please try again.");
      } catch {
        if (!cancelled) {
          setErrorMessage("Network error. Please try again.");
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    void loadCustomers();

    return () => {
      cancelled = true;
    };
  }, [clearSession]);

  const handleLogout = useCallback(async () => {
    if (logoutLoading) return;

    setLogoutLoading(true);
    setLogoutError(null);
    try {
      const result = await logoutSession();
      if (result.ok || "sessionLost" in result) {
        clearSession();
        return;
      }

      setLogoutError(result.status === 403
        ? "Logout was rejected. Please try again."
        : "Logout failed. Please try again.");
    } catch {
      setLogoutError("Network error. Logout could not be confirmed. Please try again.");
    } finally {
      setLogoutLoading(false);
    }
  }, [logoutLoading, clearSession]);

  if (loading) {
    return (
      <main className="mx-auto flex min-h-screen w-full max-w-5xl items-center justify-center px-4 py-10">
        <p className="text-sm text-slate-500">Loading customers...</p>
      </main>
    );
  }

  if (errorMessage) {
    return (
      <main className="mx-auto flex min-h-screen w-full max-w-5xl flex-col items-center justify-center gap-4 px-4 py-10">
        <p className="text-sm text-red-500">{errorMessage}</p>
        {logoutError && <p role="alert" className="text-sm text-red-500">{logoutError}</p>}
        <LogoutButton loading={logoutLoading} onLogout={() => void handleLogout()} />
      </main>
    );
  }

  if (!customers) {
    return null;
  }

  return (
    <main className="mx-auto flex min-h-screen w-full max-w-5xl flex-col gap-6 px-4 py-10">
      <section className="flex flex-wrap items-center justify-between gap-4 rounded-2xl bg-white p-6 shadow-lg">
        <div>
          <h1 className="text-2xl font-semibold text-slate-800">Customers</h1>
          <p className="mt-2 text-sm text-slate-500">
            Protected endpoint response: {customers.length} record(s)
          </p>
        </div>
        <LogoutButton loading={logoutLoading} onLogout={() => void handleLogout()} />
      </section>
      {logoutError && <p role="alert" className="text-sm text-red-500">{logoutError}</p>}
      <CustomersTable customers={customers} />
    </main>
  );
}
