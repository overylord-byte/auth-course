import { useCallback, useState } from "react";
import {
  Button,
  ButtonVariants,
  Modal,
  SubscribeCardShell,
  TextInput,
} from "@course/ui";
import { fetchCustomersPage } from "../services/basicAuthCustomerApi";

function credentialsValid(username: string, password: string): boolean {
  return username.trim().length > 0 && password.length > 0;
}

export function BasicAuthPage() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [fieldsValid, setFieldsValid] = useState(true);
  const [loading, setLoading] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const [modalDescription, setModalDescription] = useState("");

  const handleSubmit = useCallback(async () => {
    if (loading) return;
    const valid = credentialsValid(username, password);
    setFieldsValid(valid);
    if (!valid) return;

    setLoading(true);
    try {
      const result = await fetchCustomersPage(username, password);
      if (result.ok) {
        setModalDescription(
          `The API returned <b>${result.totalElements}</b> customer record(s) for this request.`,
        );
        setShowModal(true);
        setUsername("");
        setPassword("");
        setFieldsValid(true);
      } else if (result.status === 401) {
        setFieldsValid(false);
      } else {
        setModalDescription(
          `The server responded with status <b>${result.status}</b>. Check the network tab for details.`,
        );
        setShowModal(true);
      }
    } finally {
      setLoading(false);
    }
  }, [loading, username, password]);

  const formValid = credentialsValid(username, password);

  const form = (
    <>
      <TextInput
        value={username}
        label="Username"
        onValueChange={(v) => {
          setUsername(v);
          setFieldsValid(true);
        }}
        placeholder="course demo user"
        valid={fieldsValid}
        errorMessage="Valid username and password required"
        type="text"
        autoComplete="username"
        disabled={loading}
      />

      <TextInput
        value={password}
        label="Password"
        onValueChange={(v) => {
          setPassword(v);
          setFieldsValid(true);
        }}
        placeholder="password"
        valid={fieldsValid}
        errorMessage="Valid username and password required"
        type="password"
        autoComplete="current-password"
        disabled={loading}
      />

      <Button
        name={loading ? "Loading…" : "Subscribe to monthly newsletter"}
        onClick={() => void handleSubmit()}
        variant={formValid && fieldsValid ? ButtonVariants.PRIMARY : ButtonVariants.SECONDARY}
      />
    </>
  );

  return (
    <>
      <SubscribeCardShell form={form} />

      <Modal
        open={showModal}
        onClose={() => setShowModal(false)}
        title="Thanks for subscribing!"
        description={modalDescription}
        buttonText="Dismiss message"
        buttonVariant={ButtonVariants.PRIMARY}
        onButtonClick={() => setShowModal(false)}
      />
    </>
  );
}
