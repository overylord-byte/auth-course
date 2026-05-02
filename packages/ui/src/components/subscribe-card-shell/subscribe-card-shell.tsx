import type { FC, ReactNode } from "react";
import { Card } from "../card/card";
import { List } from "../list/list";
import { ListItem } from "../list/list-item";

export type SubscribeCardShellProps = {
  /** Form and actions rendered below the bullet list (same position as the original subscribe form). */
  form: ReactNode;
};

/**
 * Original newsletter layout: hero copy, checklist, banner images, and a slot for the form.
 */
export const SubscribeCardShell: FC<SubscribeCardShellProps> = ({ form }) => {
  return (
    <section className="flex min-h-screen items-center justify-center w-full px-4 py-8">
      <Card>
        <div className="flex flex-col-reverse lg:flex-row gap-16">
          <div className="flex flex-col gap-8 px-6 py-8 md:px-0 md:py-0">
            <h1 className="text-preset-1">Stay updated!</h1>
            <p className="text-preset-2">
              Join 60,000+ product managers receiving monthly updates on:
            </p>

            <List>
              <ListItem>Product discovery and building what matters</ListItem>
              <ListItem>Measuring to ensure updates are a success</ListItem>
              <ListItem>And much more!</ListItem>
            </List>

            {form}
          </div>

          <img
            src="/Banner.png"
            alt=""
            className="hidden lg:block w-[400px] h-[593px]"
          />
          <img
            src="/Banner-tablet.png"
            alt=""
            className="hidden md:block lg:hidden w-[528px] h-[358px]"
          />
          <img
            src="/Banner-mobile.png"
            alt=""
            className="block md:hidden w-full h-[284px]"
          />
        </div>
      </Card>
    </section>
  );
};
