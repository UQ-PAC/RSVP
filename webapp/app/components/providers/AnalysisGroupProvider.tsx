"use client";

import { JSX, useEffect, useReducer } from "react";
import {
  AnalysisGroupContext,
  AnalysisGroupDispatchContext,
  reducer,
} from "../../lib/context/AnalysisGroupContext";
import { useVerificationDispatch } from "../../lib/context/VerificationContext";
import { AnalysisGroup } from "../../lib/types";

interface AnalysisGroupProviderProps {
  group: AnalysisGroup;
  children?: JSX.Element | JSX.Element[];
}

export function AnalysisGroupProvider({
  group,
  children,
}: AnalysisGroupProviderProps) {
  const verificationDispatch = useVerificationDispatch();

  const [analysisGroupContext, analysisGroupDispatch] = useReducer(
    reducer,
    group,
  );

  // Update global context when group is updated
  useEffect(() => {
    if (analysisGroupContext !== group) {
      verificationDispatch({
        type: "update",
        update: analysisGroupContext,
      });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [analysisGroupContext]);

  // Update group when global context is updated
  useEffect(() => {
    if (analysisGroupContext !== group) {
      analysisGroupDispatch({ type: "update", update: group });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [group]);

  return (
    <AnalysisGroupContext value={analysisGroupContext}>
      <AnalysisGroupDispatchContext value={analysisGroupDispatch}>
        {children}
      </AnalysisGroupDispatchContext>
    </AnalysisGroupContext>
  );
}
