"use client";

import { faFileShield } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { Lexend_Deca } from "next/font/google";
import {
  ExpansionState,
  useFocus,
  useFocusDispatch,
} from "./providers/FocusContext";

const lexendDeca = Lexend_Deca({
  subsets: ["latin"],
});

interface VerifyButtonParams {
  verify: () => Promise<void>;
}

export function VerifyButton({ verify }: VerifyButtonParams) {
  const { drawer: drawerFocus } = useFocus();
  const focusDispatch = useFocusDispatch();

  const onclick = () =>
    verify().then(() => {
      if (drawerFocus["right"] === ExpansionState.Collapsed) {
        focusDispatch({
          type: "drawer",
          key: "left",
          value: ExpansionState.Collapsed,
        });
        focusDispatch({
          type: "drawer",
          key: "right",
          value: ExpansionState.Expanded,
        });
      }
    });

  return (
    <button className="verify-button" onClick={onclick}>
      <FontAwesomeIcon className="verify-button-icon" icon={faFileShield} />
      <span className={`verify-button-text ${lexendDeca.className}`}>
        Verify
      </span>
    </button>
  );
}
