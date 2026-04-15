"use client";

import { useState } from "react";
import { AnalysisGroup } from "./AnalysisGroup";
import { CreateContextButton } from "./CreateContextButton";

import "./upload.css";

export function FileUploader() {
  const [policySets, setPolicySets] = useState<string[]>([]);

  const createPolicySet = () => {
    setPolicySets([...policySets, "Policy set " + (policySets.length + 1)]);
  };

  return (
    <div className="upload-container">
      {policySets.map((policySet, i) => (
        <AnalysisGroup
          key={i}
          name={policySet}
          removeGroup={() =>
            setPolicySets(policySets.filter((name) => name !== policySet))
          }
        />
      ))}
      <CreateContextButton onclick={createPolicySet} />
    </div>
  );
}
