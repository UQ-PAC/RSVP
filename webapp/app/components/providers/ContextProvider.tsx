"use client";

import { useReducer } from "react";
import {
  emptyFocus,
  FocusContext,
  FocusDispatchContext,
  focusReducer,
} from "./FocusContext";
import {
  emptySelection,
  SelectionContext,
  SelectionDispatchContext,
  selectionReducer,
} from "./SelectionContext";
import {
  emptyVerification,
  VerificationContext,
  VerificationDispatchContext,
  verificationReducer,
} from "./VerificationContext";

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
