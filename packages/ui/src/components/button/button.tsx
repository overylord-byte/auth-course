import type { FC } from "react";
import clsx from "clsx";

export enum ButtonVariants {
  PRIMARY = "primary",
  SECONDARY = "secondary",
}

export type ButtonProps = {
  name: string;
  onClick: () => void;
  variant: ButtonVariants;
};

export const Button: FC<ButtonProps> = ({ name, onClick, variant }) => {
  const className = clsx(
    "py-4 text-white text-preset-2 rounded-md",
    variant === ButtonVariants.PRIMARY && "bg-blue-800",
    variant === ButtonVariants.SECONDARY && "bg-gradient-4",
  );

  return (
    <button type="button" className={className} onClick={onClick}>
      {name}
    </button>
  );
};
