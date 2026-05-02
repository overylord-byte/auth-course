import { Route, Routes } from "react-router-dom";
import { CustomersPage } from "./pages/CustomersPage";
import { LoginPage } from "./pages/LoginPage";
import { CustomersStateProvider } from "./state/CustomersState";

function App() {
  return (
    <CustomersStateProvider>
      <Routes>
        <Route path="/" element={<LoginPage />} />
        <Route path="/customers" element={<CustomersPage />} />
      </Routes>
    </CustomersStateProvider>
  );
}

export default App;
