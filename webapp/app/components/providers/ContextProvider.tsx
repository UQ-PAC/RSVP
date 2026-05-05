"use client";

import { useReducer } from "react";
import {
  emptyFocus,
  FocusContext,
  FocusDispatchContext,
  focusReducer,
} from "../../lib/context/FocusContext";
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

export function ContextProvider({ children }) {
  const [verificationContext, verificationDispatch] = useReducer(
    verificationReducer,
    emptyVerification,
  );
  const [focusContext, focusDispatch] = useReducer(focusReducer, emptyFocus);
  const [selectionContext, selectionDispatch] = useReducer(
    selectionReducer,
    emptySelection,
  );

  return (
    <VerificationContext value={verificationContext}>
      <VerificationDispatchContext value={verificationDispatch}>
        <FocusContext value={focusContext}>
          <FocusDispatchContext value={focusDispatch}>
            <SelectionContext value={selectionContext}>
              <SelectionDispatchContext value={selectionDispatch}>
                {children}
              </SelectionDispatchContext>
            </SelectionContext>
          </FocusDispatchContext>
        </FocusContext>
      </VerificationDispatchContext>
    </VerificationContext>
  );
}
