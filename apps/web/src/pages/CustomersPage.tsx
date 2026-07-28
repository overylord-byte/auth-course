import { useCallback, useEffect, useState } from "react";
import { Navigate, useNavigate } from "react-router-dom";
import { logoutSession } from "../api/auth/sessionAuth";
import {Customer, fetchCustomersWithSession} from "../api/customers/sessionCustomers";
import { CustomersTable } from "../components/CustomersTable";
import { LogoutButton } from "../components/LogoutButton";

export function CustomersPage() {
  const navigate = useNavigate();
  const [customers, setCustomers] = useState<Customer[] | null>(null);
  const [loading, setLoading] = useState(true);
  const [logoutLoading, setLogoutLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [unauthorized, setUnauthorized] = useState(false);

  useEffect(() => {
    let cancelled = false;

    async function loadCustomers() {
      setLoading(true);
      setErrorMessage(null);

      try {
        const result = await fetchCustomersWithSession();

        if (cancelled) return;

        if (result.ok) {
          setCustomers(result.customers);
          return;
        }

        if (result.status === 401) {
          setUnauthorized(true);
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
  }, []);

  const handleLogout = useCallback(async () => {
    if (logoutLoading) return;

    setLogoutLoading(true);
    try {
      await logoutSession();
    } catch {
      // Session cookie may already be cleared; still return to login.
    } finally {
      setLogoutLoading(false);
      navigate("/", { replace: true });
    }
  }, [logoutLoading, navigate]);

  if (unauthorized) {
    return <Navigate to="/" replace />;
  }

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
        <LogoutButton loading={logoutLoading} onLogout={() => void handleLogout()} />
      </main>
    );
  }

  if (!customers) {
    return <Navigate to="/" replace />;
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
      <CustomersTable customers={customers} />
    </main>
  );
}
