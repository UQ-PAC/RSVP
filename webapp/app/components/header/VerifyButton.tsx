"use client";

import { faFileShield } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { Lexend_Deca } from "next/font/google";
import {
  ExpansionStatus,
  useExpansion,
  useExpansionDispatch,
} from "../../lib/context/ExpansionContext";
import {
  useVerification,
  useVerificationDispatch,
} from "../../lib/context/VerificationContext";
import { publish } from "../../lib/events";
import { checkAnalysisGroup } from "../../lib/util";

const lexendDeca = Lexend_Deca({
  subsets: ["latin"],
});

export function VerifyButton() {
  const verification = useVerification();
  const verificationDispatch = useVerificationDispatch();

  const {
    drawer: { expansions },
  } = useExpansion();
  const expansionDispatch = useExpansionDispatch();

  const onclick = () => {
    publish("verificationRequested");

    const error = Object.values(verification)
      .map(({ files }) => checkAnalysisGroup(files))
      .some(({ error }) => error);

    const toOpen = error ? "left" : "right";
    const toClose = error ? "right" : "left";

    if (expansions[toOpen] === ExpansionStatus.Collapsed) {
      expansionDispatch({
        type: "toggle",
        group: "drawer",
        id: toClose,
        status: ExpansionStatus.Collapsed,
      });
      expansionDispatch({
        type: "toggle",
        group: "drawer",
        id: toOpen,
        status: ExpansionStatus.Expanded,
      });
    }

    if (!error) {
      verificationDispatch({ type: "verify" });
    }
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
