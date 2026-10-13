import { Route, Routes } from "react-router-dom";
import { CustomersPage } from "./pages/CustomersPage";
import { LoginPage } from "./pages/LoginPage";
import { AuthProvider } from "./auth/auth.provider.tsx";
import { AuthGate, GuestOnly, RequireAuth } from "./auth/auth.gate.tsx";

function App() {
  return (
    <AuthProvider>
      <AuthGate>
        <Routes>
          <Route path="/" element={<GuestOnly><LoginPage /></GuestOnly>} />
          <Route path="/customers" element={<RequireAuth><CustomersPage /></RequireAuth>} />
        </Routes>
      </AuthGate>
    </AuthProvider>
  );
}

export default App;
