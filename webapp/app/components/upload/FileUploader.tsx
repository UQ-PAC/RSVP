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

  const policySets = Object.entries(context);

  const [creating, setCreating] = useState(false);

  return (
    <div className="upload-container">
      {policySets.map(([name, group]) => (
        <AnalysisGroupProvider key={name} group={group}>
          <AnalysisGroup
            removeGroup={() => dispatch({ type: "remove", group: name })}
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
          existing={policySets.map(([name]) => name)}
        />
      )}
      {!creating && <CreateContextButton onclick={() => setCreating(true)} />}
    </div>
  );
}
