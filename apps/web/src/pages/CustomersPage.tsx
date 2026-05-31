import { useCallback, useEffect, useState } from "react";

import { Navigate, useNavigate } from "react-router-dom";

import type { Customer } from "../api/customers";

import {

  fetchCustomersWithJwt,

  logoutWithJwt,

  refreshAccessToken,

} from "../api/jwtAuth";

import { useAuth } from "../auth/AuthContext";

import { CustomersTable } from "../components/CustomersTable";

import { LogoutButton } from "../components/LogoutButton";



export function CustomersPage() {

  const navigate = useNavigate();

  const { accessToken, setAccessToken, clearAccessToken } = useAuth();

  const [customers, setCustomers] = useState<Customer[] | null>(null);

  const [sessionReady, setSessionReady] = useState(false);

  const [loading, setLoading] = useState(true);

  const [logoutLoading, setLogoutLoading] = useState(false);

  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const [unauthorized, setUnauthorized] = useState(false);



  useEffect(() => {

    let cancelled = false;



    async function bootstrapSession() {

      if (accessToken) {

        setSessionReady(true);

        return;

      }



      try {

        const refreshResult = await refreshAccessToken();



        if (cancelled) {

          return;

        }



        if (refreshResult.ok) {

          setAccessToken(refreshResult.accessToken);

        } else {

          setUnauthorized(true);

        }

      } catch {

        if (!cancelled) {

          setUnauthorized(true);

        }

      } finally {

        if (!cancelled) {

          setSessionReady(true);

        }

      }

    }



    void bootstrapSession();



    return () => {

      cancelled = true;

    };

  }, [accessToken, setAccessToken]);



  useEffect(() => {

    if (!sessionReady || !accessToken || unauthorized) {

      return;

    }



    const token = accessToken;

    let cancelled = false;



    async function loadCustomers() {

      setLoading(true);

      setErrorMessage(null);



      try {

        const result = await fetchCustomersWithJwt(token);



        if (cancelled) return;



        if (result.ok) {

          setCustomers(result.customers);

          return;

        }



        if (result.status === 401) {

          clearAccessToken();

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

  }, [accessToken, clearAccessToken, sessionReady, unauthorized]);



  const handleLogout = useCallback(async () => {

    if (logoutLoading) return;



    setLogoutLoading(true);



    try {

      await logoutWithJwt();

    } catch {

      // Even if logout request fails, clear local access token and redirect.

    } finally {

      clearAccessToken();

      setLogoutLoading(false);

      navigate("/", { replace: true });

    }

  }, [clearAccessToken, logoutLoading, navigate]);



  if (!sessionReady) {

    return (

      <main className="mx-auto flex min-h-screen w-full max-w-5xl items-center justify-center px-4 py-10">

        <p className="text-sm text-slate-500">Restoring session...</p>

      </main>

    );

  }



  if (!accessToken || unauthorized) {

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

