"use client";

import { useVerification } from "../../lib/context/VerificationContext";
import { Fallback } from "./Fallback";

import { AnalysisGroupProvider } from "../providers/AnalysisGroupProvider";
import { AnalysisGroup } from "./AnalysisGroup";
import "./sources.css";

export function SourceFileViewer() {
  const verificationContext = useVerification();
  const groups = Object.values(verificationContext);

  return (
    <div className="source-files-container">
      {groups.map((group) => {
        return (
          <AnalysisGroupProvider key={group.name} group={group}>
            <AnalysisGroup />
          </AnalysisGroupProvider>
        );
      })}
      {!groups.length && <Fallback instruction="Create a policy set" />}
    </div>
  );
}
