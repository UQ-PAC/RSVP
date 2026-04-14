import { faCodeCompare, faXmark } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { JSX } from "react";
import { VerificationFile } from "../../types";

interface UploadedFileProps {
  file: VerificationFile;
  remove: (file: VerificationFile) => void;
  children?: JSX.Element[];
  addChild?: (file: VerificationFile) => void;
}

export function UploadedFile({
  file,
  children,
  remove,
  addChild,
}: UploadedFileProps) {
  return (
    <div className="uploaded-file-container">
      <span className="uploaded-file">
        <span className="uploaded-file-name">{file.file.name}</span>
        {addChild && (
          <FontAwesomeIcon
            className="uploaded-file-version-icon"
            icon={faCodeCompare}
            onClick={() => addChild(file)}
          />
        )}
        <FontAwesomeIcon
          className="uploaded-file-delete-icon"
          icon={faXmark}
          onClick={() => remove(file)}
        />
      </span>
      <div className="uploaded-file-versions">{children}</div>
    </div>
  );
}
