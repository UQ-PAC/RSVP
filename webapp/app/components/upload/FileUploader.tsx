"use client";

import { useState } from "react";
import { AnalysisGroup } from "./AnalysisGroup";
import { CreateContextButton } from "./CreateContextButton";
import { useVerificationDispatch } from "../providers/VerificationContext";
import { upload } from "../../requests";
import { getFileType } from "@/app/util";

export function FileUploader() {
  const [policySets, setPolicySets] = useState<string[]>([]);

  const dispatch = useVerificationDispatch();

  const createPolicySet = () => {
    setPolicySets([...policySets, "Policy set " + (policySets.length + 1)]);
  };

  return (
    <div className="upload-container">
      {policySets.map((policySet, i) => (
        <AnalysisGroup
          key={i}
          name={policySet}
          addFiles={(toAdd: File[]) => {
            toAdd.forEach((file) => {
              dispatch({
                type: "add",
                group: policySet,
                file: {
                  file,
                  filetype: getFileType(file) ?? "cedar",
                  resolved: upload(file),
                },
              });
            });
          }}
          removeGroup={() =>
            setPolicySets(policySets.filter((name) => name !== policySet))
          }
        />
      ))}
      <CreateContextButton onclick={createPolicySet} />
    </div>
  );
}
