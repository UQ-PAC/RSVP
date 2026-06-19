"use client";

import { useReducer } from "react";
import {
  emptyExpansionState,
  ExpansionContext,
  ExpansionDispatchContext,
  expansionReducer,
} from "../../lib/context/ExpansionContext";
import {
  emptySelection,
  SelectionContext,
  SelectionDispatchContext,
  selectionReducer,
} from "../../lib/context/SelectionContext";
import {
  emptyVerification,
  VerificationContext,
  VerificationDispatchContext,
  verificationReducer,
} from "../../lib/context/VerificationContext";
import { useEventListener } from "../../lib/events";

export function ApplicationStateProvider({ children }) {
  const [verificationContext, verificationDispatch] = useReducer(
    verificationReducer,
    emptyVerification,
  );
  const [expansionContext, expansionDispatch] = useReducer(
    expansionReducer,
    emptyExpansionState,
  );
  const [selectionContext, selectionDispatch] = useReducer(
    selectionReducer,
    emptySelection,
  );

  useEventListener("verificationComplete", () => {
    verificationDispatch({ type: "complete" });
  });

  return (
    <VerificationContext value={verificationContext}>
      <VerificationDispatchContext value={verificationDispatch}>
        <ExpansionContext value={expansionContext}>
          <ExpansionDispatchContext value={expansionDispatch}>
            <SelectionContext value={selectionContext}>
              <SelectionDispatchContext value={selectionDispatch}>
                {children}
              </SelectionDispatchContext>
            </SelectionContext>
          </ExpansionDispatchContext>
        </ExpansionContext>
      </VerificationDispatchContext>
    </VerificationContext>
  );
}
