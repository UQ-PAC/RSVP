"use client";

import { useState } from "react";
import { AnalysisGroup } from "./AnalysisGroup";
import { CreateContextButton } from "./CreateContextButton";

import { NewGroupForm } from "./NewGroupForm";
import "./upload.css";

export function FileUploader() {
  const [policySets, setPolicySets] = useState<string[]>([]);
  const [creating, setCreating] = useState(false);

  const openCreatePolicySetForm = () => {
    setCreating(true);
  };

  const cancelCreatePolicySet = () => {
    setCreating(false);
  };

  const createPolicySet = (name: string) => {
    setPolicySets([...policySets, name]);
    setCreating(false);
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
      {creating && (
        <NewGroupForm
          index={policySets.length + 1}
          create={createPolicySet}
          cancel={cancelCreatePolicySet}
          existing={[...policySets]}
        />
      )}
      {!creating && <CreateContextButton onclick={openCreatePolicySetForm} />}
    </div>
  );
}
