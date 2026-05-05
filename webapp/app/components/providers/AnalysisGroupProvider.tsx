"use client";

import { JSX, useEffect, useReducer } from "react";
import {
  AnalysisGroupContext,
  AnalysisGroupDispatchContext,
  emptyAnalysisGroup,
  reducer,
} from "../../lib/context/AnalysisGroupContext";
import {
  useVerification,
  useVerificationDispatch,
} from "../../lib/context/VerificationContext";

interface AnalysisGroupProviderProps {
  group: string;
  children?: JSX.Element | JSX.Element[];
}

export function AnalysisGroupProvider({
  group,
  children,
}: AnalysisGroupProviderProps) {
  const verificationContext = useVerification();
  const verificationDispatch = useVerificationDispatch();

  const globalGroupContext = verificationContext[group];

  const [analysisGroupContext, analysisGroupDispatch] = useReducer(
    reducer,
    globalGroupContext ?? emptyAnalysisGroup,
  );

  // Update global context when group is updated
  useEffect(() => {
    verificationDispatch({
      type: "update",
      group,
      update: analysisGroupContext,
    });
  }, [analysisGroupContext, group, verificationDispatch]);

  // Update group when global context is updated
  useEffect(() => {
    analysisGroupDispatch({ type: "update", update: globalGroupContext });
  }, [globalGroupContext]);

  return (
    <AnalysisGroupContext value={analysisGroupContext}>
      <AnalysisGroupDispatchContext value={analysisGroupDispatch}>
        {children}
      </AnalysisGroupDispatchContext>
    </AnalysisGroupContext>
  );
}
