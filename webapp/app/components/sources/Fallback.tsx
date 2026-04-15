"use client";

import {
  ExpansionState,
  useFocus,
  useFocusDispatch,
} from "../providers/FocusContext";

interface FallbackProps {
  instruction: string;
}

export function Fallback({ instruction }: FallbackProps) {
  const { drawer: drawerFocus } = useFocus();
  const focusDispatch = useFocusDispatch();

  return (
    <p className="source-files-instruction">
      <a
        className="source-files-upload-link"
        onClick={() => {
          if (drawerFocus["left"] == ExpansionState.Collapsed) {
            focusDispatch({
              type: "drawer",
              key: "left",
              value: ExpansionState.Expanded,
            });
            focusDispatch({
              type: "drawer",
              key: "right",
              value: ExpansionState.Collapsed,
            });
          }
        }}
      >
        {instruction}
      </a>{" "}
      to run verification.
    </p>
  );
}
