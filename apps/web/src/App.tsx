import { Route, Routes } from "react-router-dom";
import { CustomersPage } from "./pages/CustomersPage";
import { LoginPage } from "./pages/LoginPage";

function App() {
  return (
    <Routes>
      <Route path="/" element={<LoginPage />} />
      <Route path="/customers" element={<CustomersPage />} />
    </Routes>
  );
}

export default App;
