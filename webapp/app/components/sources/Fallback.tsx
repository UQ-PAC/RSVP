"use client";

import {
  ExpansionStatus,
  useExpansion,
  useExpansionDispatch,
} from "../../lib/context/ExpansionContext";

interface FallbackProps {
  instruction: string;
  target?: string;
}

export function Fallback({ instruction, target }: FallbackProps) {
  const { drawer: drawerFocus } = useExpansion();
  const expansionDispatch = useExpansionDispatch();

  return (
    <p className="source-files-instruction">
      <a
        className="source-files-upload-link"
        data-testid="source-files-upload-link"
        onClick={() => {
          if (drawerFocus.expansions.left == ExpansionStatus.Collapsed) {
            expansionDispatch({
              type: "toggle",
              group: "drawer",
              id: "left",
              status: ExpansionStatus.Expanded,
            });
            expansionDispatch({
              type: "toggle",
              group: "drawer",
              id: "right",
              status: ExpansionStatus.Collapsed,
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
