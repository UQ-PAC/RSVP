"use client";

import { faCaretLeft, faCaretRight } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import cx from "classnames";
import {
  ExpansionState,
  useFocus,
  useFocusDispatch,
} from "./providers/FocusContext";

import "./drawer.css";

interface DrawerProps {
  title: string;
  side: "left" | "right";
  children?: React.ReactNode;
}

export function Drawer({ title, side, children }: DrawerProps) {
  const { drawer } = useFocus();
  const dispatch = useFocusDispatch();

  const expanded = !drawer[side];

  const className = cx("drawer", `drawer-${side}`, expanded && "expanded");

  const icon =
    (side === "left" && expanded) || (side === "right" && !expanded)
      ? faCaretLeft
      : faCaretRight;

  return (
    <div className={className}>
      <div
        className="drawer-tab"
        onClick={() =>
          dispatch({
            type: "drawer",
            key: side,
            value: expanded
              ? ExpansionState.Collapsed
              : ExpansionState.Expanded,
          })
        }
      >
        <FontAwesomeIcon className="drawer-tab-icon" icon={icon} />
      </div>
      <div className="drawer-container">
        <h3 className="drawer-title">{title}</h3>
        <div className="drawer-content">{children}</div>
      </div>
    </div>
  );
}
