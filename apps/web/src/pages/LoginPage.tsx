import { useCallback, useState } from "react";
import { useNavigate } from "react-router-dom";
import { fetchCustomersWithBasicAuth } from "../api/customers";
import { LoginForm } from "../components/LoginForm";
import { useCustomersState } from "../state/CustomersState";

function credentialsValid(username: string, password: string): boolean {
  return username.trim().length > 0 && password.length > 0;
}

export function LoginPage() {
  const navigate = useNavigate();
  const { setCustomers } = useCustomersState();

  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [fieldsValid, setFieldsValid] = useState(true);
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const handleSubmit = useCallback(async () => {
    if (loading) return;

    const valid = credentialsValid(username, password);
    setFieldsValid(valid);
    if (!valid) {
      setErrorMessage("Enter username and password.");
      return;
    }

    setErrorMessage(null);
    setLoading(true);

    try {
      const result = await fetchCustomersWithBasicAuth(username, password);

      if (!result.ok) {
        if (result.status === 401) {
          setFieldsValid(false);
          setErrorMessage("Invalid credentials. Please check your username and password.");
          return;
        }

        setErrorMessage("Login failed. Please try again.");
        return;
      }

      setCustomers(result.customers);
      setUsername("");
      setPassword("");
      navigate("/customers");
    } catch {
      setErrorMessage("Network error. Please try again.");
    } finally {
      setLoading(false);
    }
  }, [loading, navigate, password, setCustomers, username]);

  return (
    <LoginForm
      username={username}
      password={password}
      fieldsValid={fieldsValid}
      loading={loading}
      errorMessage={errorMessage}
      onUsernameChange={(value) => {
        setUsername(value);
        setFieldsValid(true);
        setErrorMessage(null);
      }}
      onPasswordChange={(value) => {
        setPassword(value);
        setFieldsValid(true);
        setErrorMessage(null);
      }}
      onSubmit={() => void handleSubmit()}
    />
  );
}
