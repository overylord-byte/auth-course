import type { Customer } from "../api/customers";

type CustomersTableProps = {
  customers: Customer[];
};

export function CustomersTable({ customers }: CustomersTableProps) {
  return (
    <div className="w-full overflow-hidden rounded-2xl border border-slate-600 bg-white">
      <table className="w-full border-collapse text-left">
        <thead className="bg-slate-100 text-slate-700">
          <tr>
            <th className="px-4 py-3 font-semibold">ID</th>
            <th className="px-4 py-3 font-semibold">Name</th>
          </tr>
        </thead>
        <tbody>
          {customers.map((customer) => (
            <tr key={customer.id} className="border-t border-slate-200">
              <td className="px-4 py-3 text-sm text-slate-600">{customer.id}</td>
              <td className="px-4 py-3 text-slate-800">{customer.name}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
