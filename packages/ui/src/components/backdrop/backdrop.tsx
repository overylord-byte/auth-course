import type { FC, PropsWithChildren } from "react";

export type BackdropProps = PropsWithChildren<{
  titleId: string;
  descId: string;
}>;

export const Backdrop: FC<BackdropProps> = ({ descId, titleId, children }) => {
  return (
    <div
      className="fixed inset-0 z-[1000]"
      aria-labelledby={titleId}
      aria-describedby={descId}
      role="dialog"
      aria-modal="true"
    >
      {children}
    </div>
  );
};
