"use client";

import {
  ExpansionState,
  useFocus,
  useFocusDispatch,
} from "../providers/FocusContext";

interface FallbackProps {
  instruction: string;
  target?: string;
}

export function Fallback({ instruction, target }: FallbackProps) {
  const { drawer: drawerFocus } = useFocus();
  const focusDispatch = useFocusDispatch();

  return (
    <p className="source-files-instruction">
      <a
        className="source-files-upload-link"
        onClick={() => {
          if (drawerFocus.expansions["left"] == ExpansionState.Collapsed) {
            focusDispatch({
              type: "focus",
              target: "drawer",
              focus: { key: "left", value: ExpansionState.Expanded },
            });
            focusDispatch({
              type: "focus",
              target: "drawer",
              focus: { key: "right", value: ExpansionState.Collapsed },
            });
          }
        }}
      >
        {instruction}
      </a>{" "}
      to run verification{target ? ` on ${target}` : ""}.
    </p>
  );
}
