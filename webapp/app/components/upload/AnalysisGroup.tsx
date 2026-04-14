import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { FileInput } from "./FileInput";
import { faTrashCan } from "@fortawesome/free-regular-svg-icons";
import {
  emptyVerificationGroup,
  useVerification,
  useVerificationDispatch,
} from "../providers/VerificationContext";
import { VerificationFile } from "@/app/types";
import { remove } from "../../requests";
import { FileList } from "./FileList";

interface AnalysisGroupProps {
  name: string;
  addFiles: (files: File[]) => void;
  removeGroup: () => void;
}

export function AnalysisGroup({
  name,
  addFiles,
  removeGroup,
}: AnalysisGroupProps) {
  const context = useVerification();
  const dispatch = useVerificationDispatch();

  const {
    cedar: policies,
    cedarschema: schemas,
    entities,
    invariant: invariants,
  } = context[name] ?? emptyVerificationGroup;

  const removeFile = (file: VerificationFile) => {
    file.resolved.then((uploaded) => remove(uploaded.serverId));
    dispatch({ type: "remove", group: name, file: file });
  };

  return (
    <div className="analysis-group">
      <span className="analysis-group-header">
        <h4 className="analysis-group-title">{name}</h4>
        <FontAwesomeIcon
          className="analysis-group-delete-icon"
          icon={faTrashCan}
          onClick={removeGroup}
        />
      </span>
      <div className="analysis-group-filelist">
        {policies.length > 0 && (
          <FileList
            title="Policies"
            filetype="cedar"
            group={name}
            files={policies}
            remove={removeFile}
            nested
          />
        )}
        {schemas.length > 0 && (
          <FileList
            title="Schemas"
            filetype="cedarschema"
            group={name}
            files={schemas}
            remove={removeFile}
          />
        )}
        {entities.length > 0 && (
          <FileList
            title="Entities"
            filetype="entities"
            group={name}
            files={entities}
            remove={removeFile}
          />
        )}
        {invariants.length > 0 && (
          <FileList
            title="Invariants"
            filetype="invariant"
            group={name}
            files={invariants}
            remove={removeFile}
          />
        )}
      </div>
      <FileInput addFiles={addFiles} />
    </div>
  );
}
