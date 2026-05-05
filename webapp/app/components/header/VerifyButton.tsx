"use client";

import { faFileShield } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { Lexend_Deca } from "next/font/google";
import {
  ExpansionState,
  useFocus,
  useFocusDispatch,
} from "../../lib/context/FocusContext";
import {
  useVerification,
  useVerificationDispatch,
} from "../../lib/context/VerificationContext";
import { checkAnalysisGroup } from "../../lib/util";

const lexendDeca = Lexend_Deca({
  subsets: ["latin"],
});

export function VerifyButton() {
  const verification = useVerification();
  const verificationDispatch = useVerificationDispatch();

  const {
    drawer: { expansions },
  } = useFocus();
  const focusDispatch = useFocusDispatch();

  const onclick = () => {
    const error = Object.values(verification)
      .map(({ files }) => checkAnalysisGroup(files))
      .some(({ error }) => error);

    const toOpen = error ? "left" : "right";
    const toClose = error ? "right" : "left";

    if (expansions[toOpen] === ExpansionState.Collapsed) {
      focusDispatch({
        type: "focus",
        target: "drawer",
        focus: { key: toClose, value: ExpansionState.Collapsed },
      });
      focusDispatch({
        type: "focus",
        target: "drawer",
        focus: { key: toOpen, value: ExpansionState.Expanded },
      });
    }

    verificationDispatch({ type: "pre-verify" });
    verificationDispatch({ type: "verify" });
  };

  return (
    <button
      className="verify-button"
      onClick={onclick}
      data-testid="verify-button"
    >
      <FontAwesomeIcon className="verify-button-icon" icon={faFileShield} />
      <span className={`verify-button-text ${lexendDeca.className}`}>
        Verify
      </span>
    </button>
  );
}
