import { ExpansionStatus } from "../../lib/context/ExpansionContext";

import "./toggle.css";

interface ToggleAllProps {
  name: string;
  toggle: (expand: ExpansionStatus) => void;
}

export function ToggleAll({ name, toggle }: ToggleAllProps) {
  return (
    <span className={`toggle-all ${name}-toggle-all`}>
      <a
        className={`expand-all ${name}-expand-all`}
        data-testid="expand-all"
        onClick={() => toggle(ExpansionStatus.Expanded)}
      >
        EXPAND ALL
      </a>
      <span>|</span>
      <a
        className={`collapse-all ${name}-collapse-all`}
        data-testid="collapse-all"
        onClick={() => toggle(ExpansionStatus.Collapsed)}
      >
        COLLAPSE ALL
      </a>
    </span>
  );
}
