"use client";

import { useState } from "react";
import { VerificationContext } from "./VerificationContext";
import { CreateContextButton } from "./CreateContextButton";
import { useVerificationDispatch } from "../providers/VerificationContext";
import { upload } from "../../requests";
import { FileType } from "@/app/types";

export function FileUploader() {
  const [policySets, setPolicySets] = useState<string[]>([]);

  const dispatch = useVerificationDispatch();

  const createPolicySet = () => {
    setPolicySets([
      ...policySets,
      "Verification context " + (policySets.length + 1),
    ]);
  };

  return (
    <div className="files-container">
      {policySets.map((policySet, i) => (
        <VerificationContext
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
        />
      ))}
      <CreateContextButton onclick={createPolicySet} />
    </div>
  );
}
