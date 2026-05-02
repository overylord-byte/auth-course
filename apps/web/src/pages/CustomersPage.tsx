import { Navigate } from "react-router-dom";
import { CustomersTable } from "../components/CustomersTable";
import { useCustomersState } from "../state/CustomersState";

export function CustomersPage() {
  const { customers } = useCustomersState();

  if (!customers) {
    return <Navigate to="/" replace />;
  }

  return (
    <main className="mx-auto flex min-h-screen w-full max-w-5xl flex-col gap-6 px-4 py-10">
      <section className="rounded-2xl bg-white p-6 shadow-lg">
        <h1 className="text-2xl font-semibold text-slate-800">Customers</h1>
        <p className="mt-2 text-sm text-slate-500">Protected endpoint response: {customers.length} record(s)</p>
      </section>
      <CustomersTable customers={customers} />
    </main>
  );
}
