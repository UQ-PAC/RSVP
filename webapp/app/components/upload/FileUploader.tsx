"use client";

import { useState } from "react";
import { AnalysisGroup } from "./AnalysisGroup";
import { CreateContextButton } from "./CreateContextButton";

import {
  useVerification,
  useVerificationDispatch,
} from "../providers/VerificationContext";
import { NewGroupForm } from "./NewGroupForm";
import "./upload.css";

export function FileUploader() {
  const context = useVerification();
  const dispatch = useVerificationDispatch();
  const policySets = Object.keys(context);

  const [creating, setCreating] = useState(false);

  const openCreatePolicySetForm = () => {
    setCreating(true);
  };

  const cancelCreatePolicySet = () => {
    setCreating(false);
  };

  const createPolicySet = (name: string) => {
    dispatch({ type: "add", group: name });
    setCreating(false);
  };

  return (
    <div className="upload-container">
      {policySets.map((policySet, i) => (
        <AnalysisGroup
          key={i}
          name={policySet}
          removeGroup={() => dispatch({ type: "remove", group: policySet })}
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
