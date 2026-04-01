"use client";

import { useState } from "react";
import { VerificationGroup } from "./VerificationGroup";
import { CreateContextButton } from "./CreateContextButton";
import { useVerificationDispatch } from "../providers/VerificationContext";
import { upload } from "../../requests";
import { FileType } from "../../types";

export function FileUploader() {
  const [policySets, setPolicySets] = useState<string[]>([]);

  const dispatch = useVerificationDispatch();

  const createPolicySet = () => {
    setPolicySets([...policySets, "Policy set " + (policySets.length + 1)]);
  };

  return (
    <div className="upload-container">
      {policySets.map((policySet, i) => (
        <VerificationGroup
          key={i}
          name={policySet}
          addFiles={(toAdd: File[]) => {
            toAdd.forEach((file) => {
              let filetype: FileType = "cedar";

              if (file.name.endsWith(".cedarschema")) {
                filetype = "cedarschema";
              } else if (file.name.endsWith(".json")) {
                filetype = "entities";
              } else if (file.name.endsWith(".invariant")) {
                filetype = "invariant";
              } else if (!file.name.endsWith("cedar")) {
                return;
              }

              dispatch({
                type: "add",
                group: policySet,
                file: {
                  file,
                  filetype,
                  resolved: upload(file),
                },
              });
            });
          }}
          remove={() =>
            setPolicySets(policySets.filter((name) => name !== policySet))
          }
        />
      ))}
      <CreateContextButton onclick={createPolicySet} />
    </div>
  );
}
