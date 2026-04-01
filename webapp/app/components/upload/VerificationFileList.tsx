"use client";

import { FileGroup } from "./FileGroup";
import { VerificationFile } from "../../types";
import {
  emptyVerificationGroup,
  useVerification,
  useVerificationDispatch,
} from "../providers/VerificationContext";
import { remove } from "../../requests";

interface VerificationFileListProps {
  group: string;
}

export function VerificationFileList({ group }: VerificationFileListProps) {
  const context = useVerification();
  const dispatch = useVerificationDispatch();

  const {
    cedar: policies,
    cedarschema: schemas,
    entities,
    invariant: invariants,
  } = context[group] ?? emptyVerificationGroup;

  const removeFile = (file: VerificationFile) => {
    file.resolved.then((uploaded) => remove(uploaded.serverId));
    dispatch({ type: "remove", group, file: file });
  };

  return (
    <div className="verification-context-filelist">
      {policies.length > 0 && (
        <FileGroup
          title="Policies"
          filetype="cedar"
          files={policies}
          remove={removeFile}
        />
      )}
      {schemas.length > 0 && (
        <FileGroup
          title="Schemas"
          filetype="cedarschema"
          files={schemas}
          remove={removeFile}
        />
      )}
      {entities.length > 0 && (
        <FileGroup
          title="Entities"
          filetype="entities"
          files={entities}
          remove={removeFile}
        />
      )}
      {invariants.length > 0 && (
        <FileGroup
          title="Invariants"
          filetype="invariant"
          files={invariants}
          remove={removeFile}
        />
      )}
    </div>
  );
}
