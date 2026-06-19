import cx from "classnames";
import { JSX, Ref } from "react";

export type TabTheme = "dark" | "light";

interface TabParams {
  select: () => void;
  selected?: boolean;
  theme?: TabTheme;
  ref?: Ref<HTMLDivElement>;
  children?: JSX.Element | JSX.Element[];
}

export function Tab({
  selected = false,
  select,
  theme = "light",
  ref,
  children,
}: TabParams) {
  return (
    <div
      ref={selected ? ref : undefined}
      className={cx("tab", selected && "selected", theme)}
      onClick={(e) => {
        e.stopPropagation();
        if (!selected) {
          select();
        }
      }}
      onMouseOver={(e) => {
        e.stopPropagation();
      }}
      data-testid="tab"
    >
      {children}
    </div>
  );
}
