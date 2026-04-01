import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { FileInput } from "./FileInput";
import { VerificationFileList } from "./VerificationFileList";
import { faTrashCan } from "@fortawesome/free-regular-svg-icons";

interface VerificationGroupProps {
  name: string;
  addFiles: (files: File[]) => void;
  remove: () => void;
}

export function VerificationGroup({
  name,
  addFiles,
  remove,
}: VerificationGroupProps) {
  return (
    <div className="verification-context">
      <span className="verification-context-header">
        <h4 className="verification-context-title">{name}</h4>
        <FontAwesomeIcon
          className="verification-context-delete-icon"
          icon={faTrashCan}
          onClick={remove}
        />
      </span>
      <VerificationFileList group={name} />
      <FileInput addFiles={addFiles} />
    </div>
  );
}
