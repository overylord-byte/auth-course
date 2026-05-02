import { createContext, useContext, useMemo, useState, type ReactNode } from "react";
import type { Customer } from "../api/customers";

type CustomersStateContextValue = {
  customers: Customer[] | null;
  setCustomers: (value: Customer[]) => void;
  clearCustomers: () => void;
};

const CustomersStateContext = createContext<CustomersStateContextValue | undefined>(undefined);

export function CustomersStateProvider({ children }: { children: ReactNode }) {
  const [customers, setCustomersState] = useState<Customer[] | null>(null);

  const value = useMemo<CustomersStateContextValue>(
    () => ({
      customers,
      setCustomers: (value) => setCustomersState(value),
      clearCustomers: () => setCustomersState(null),
    }),
    [customers],
  );

  return <CustomersStateContext.Provider value={value}>{children}</CustomersStateContext.Provider>;
}

export function useCustomersState(): CustomersStateContextValue {
  const context = useContext(CustomersStateContext);
  if (!context) {
    throw new Error("useCustomersState must be used inside CustomersStateProvider");
  }
  return context;
}
