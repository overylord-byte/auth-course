import { Button, ButtonVariants } from "@course/ui";

type LogoutButtonProps = {
  loading: boolean;
  onLogout: () => void;
};

export function LogoutButton({ loading, onLogout }: LogoutButtonProps) {
  return (
    <Button
      name={loading ? "Logging out..." : "Logout"}
      onClick={onLogout}
      variant={ButtonVariants.SECONDARY}
    />
  );
}
