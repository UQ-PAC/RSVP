"use client";

import { useState } from "react";
import { AnalysisGroup } from "./AnalysisGroup";
import { CreateContextButton } from "./CreateContextButton";

import {
  useVerification,
  useVerificationDispatch,
} from "../../lib/context/VerificationContext";
import { AnalysisGroupProvider } from "../providers/AnalysisGroupProvider";
import { NewGroupForm } from "./NewGroupForm";
import "./upload.css";

export function FileUploader() {
  const context = useVerification();
  const dispatch = useVerificationDispatch();

  const policySets = Object.keys(context);

  const [creating, setCreating] = useState(false);

  return (
    <div className="upload-container">
      {policySets.map((policySet, i) => (
        <AnalysisGroupProvider key={i} group={policySet}>
          <AnalysisGroup
            removeGroup={() => dispatch({ type: "remove", group: policySet })}
          />
        </AnalysisGroupProvider>
      ))}
      {creating && (
        <NewGroupForm
          index={policySets.length + 1}
          create={(name) => {
            dispatch({ type: "add", group: name });
            setCreating(false);
          }}
          cancel={() => setCreating(false)}
          existing={[...policySets]}
        />
      )}
      {!creating && <CreateContextButton onclick={() => setCreating(true)} />}
    </div>
  );
}
