"use client";

import { faFileShield } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { Lexend_Deca } from "next/font/google";
import {
  ExpansionState,
  useFocus,
  useFocusDispatch,
} from "../providers/FocusContext";
import { useVerificationDispatch } from "../providers/VerificationContext";

const lexendDeca = Lexend_Deca({
  subsets: ["latin"],
});

export function VerifyButton() {
  const { drawer: drawerFocus } = useFocus();
  const focusDispatch = useFocusDispatch();
  const verificationDispatch = useVerificationDispatch();

  const onclick = () => {
    verificationDispatch({ type: "verify" });

    if (drawerFocus.expansions["right"] === ExpansionState.Collapsed) {
      focusDispatch({
        type: "focus",
        target: "drawer",
        focus: { key: "left", value: ExpansionState.Collapsed },
      });
      focusDispatch({
        type: "focus",
        target: "drawer",
        focus: { key: "right", value: ExpansionState.Expanded },
      });
    }
  };

  return (
    <button className="verify-button" onClick={onclick}>
      <FontAwesomeIcon className="verify-button-icon" icon={faFileShield} />
      <span className={`verify-button-text ${lexendDeca.className}`}>
        Verify
      </span>
    </button>
  );
}
