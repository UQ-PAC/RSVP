import { faCaretLeft, faCaretRight } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import cx from "classnames";

interface DrawerProps {
  title: string;
  side: "left" | "right";
  expanded?: boolean;
  setExpanded: (boolean) => void;
  children?: React.ReactNode;
}

export function Drawer({
  title,
  side,
  expanded,
  setExpanded,
  children,
}: DrawerProps) {
  const className = cx(
    "drawer-container",
    `drawer-container-${side}`,
    expanded && "expanded",
  );

  const icon =
    (side === "left" && expanded) || (side === "right" && !expanded)
      ? faCaretLeft
      : faCaretRight;

  return (
    <div className={className}>
      <div className="drawer-tab" onClick={() => setExpanded(!expanded)}>
        <FontAwesomeIcon className="drawer-tab-icon" icon={icon} />
      </div>
      <div className="drawer-content">
        <h3 className="drawer-title ">{title}</h3>
        {children}
      </div>
    </div>
  );
}
