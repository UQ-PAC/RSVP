import { ExpansionState } from "../providers/FocusContext";

import "./toggle.css";

interface ToggleAllProps {
  name: string;
  toggle: (expand: ExpansionState) => void;
}

export function ToggleAll({ name, toggle }: ToggleAllProps) {
  return (
    <span className={`toggle-all ${name}-toggle-all`}>
      <a
        className={`expand-all ${name}-expand-all`}
        onClick={() => toggle(ExpansionState.Expanded)}
      >
        EXPAND ALL
      </a>
      <span>|</span>
      <a
        className={`collapse-all ${name}-collapse-all`}
        onClick={() => toggle(ExpansionState.Collapsed)}
      >
        COLLAPSE ALL
      </a>
    </span>
  );
}
