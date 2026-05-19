import { Button, ButtonVariants, SubscribeCardShell, TextInput } from "@course/ui";

type LoginFormProps = {
  username: string;
  password: string;
  fieldsValid: boolean;
  loading: boolean;
  errorMessage: string | null;
  onUsernameChange: (value: string) => void;
  onPasswordChange: (value: string) => void;
  onSubmit: () => void;
};

export function LoginForm({
  username,
  password,
  fieldsValid,
  loading,
  errorMessage,
  onUsernameChange,
  onPasswordChange,
  onSubmit,
}: LoginFormProps) {
  const formValid = username.trim().length > 0 && password.length > 0;

  const form = (
    <>
      <TextInput
        value={username}
        label="Username"
        onValueChange={onUsernameChange}
        placeholder="Username"
        valid={fieldsValid}
        errorMessage="Valid username and password required"
        type="text"
        autoComplete="username"
        disabled={loading}
      />

      <TextInput
        value={password}
        label="Password"
        onValueChange={onPasswordChange}
        placeholder="Password"
        valid={fieldsValid}
        errorMessage="Valid username and password required"
        type="password"
        autoComplete="current-password"
        disabled={loading}
      />

      {errorMessage ? <p className="text-sm text-red-500">{errorMessage}</p> : null}

      <Button
        name={loading ? "Loading..." : "Login"}
        onClick={onSubmit}
        variant={formValid && fieldsValid ? ButtonVariants.PRIMARY : ButtonVariants.SECONDARY}
      />
    </>
  );

  return <SubscribeCardShell form={form} />;
}
