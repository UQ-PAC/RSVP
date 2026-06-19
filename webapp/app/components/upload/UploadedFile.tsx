import { faCodeCompare, faXmark } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { JSX } from "react";
import {
  ExpansionStatus,
  useExpansionDispatch,
} from "../../lib/context/ExpansionContext";
import { useSelectionDispatch } from "../../lib/context/SelectionContext";
import { getFileIcon } from "../../lib/fa-util";
import { VerificationFile, VersionedFile } from "../../lib/types";

interface UploadedFileProps {
  file: VerificationFile;
  remove: (file: VerificationFile) => void;
  children?: JSX.Element | JSX.Element[];
  addChild?: (file: VersionedFile) => void;
  icon?: boolean;
  version?: number;
  original?: VersionedFile;
}

export function UploadedFile({
  file,
  children,
  remove,
  addChild,
  icon = false,
  version,
  original,
}: UploadedFileProps) {
  const expansionDispatch = useExpansionDispatch();
  const selectionDispatch = useSelectionDispatch();

  return (
    <div className="uploaded-file-container">
      <span className="uploaded-file">
        <span
          className="uploaded-file-name"
          onClick={() => {
            file.resolved.then(({ serverId }) => {
              expansionDispatch({
                type: "toggle",
                group: "source-file",
                id: serverId,
                status: ExpansionStatus.Expanded,
              });
              selectionDispatch({
                scroll: "file",
                file: serverId,
              });
            });
          }}
        >
          {icon && (
            <FontAwesomeIcon
              className="uploaded-filetype-icon"
              icon={getFileIcon(file.filetype)}
            />
          )}
          {version !== undefined && (
            <span className="uploaded-file-version-indicator">{`Version ${version}`}</span>
          )}
          {file.filename}
        </span>
        {addChild && original && (
          <div className="action-icon uploaded-file-version-icon">
            <FontAwesomeIcon
              icon={faCodeCompare}
              onClick={() => addChild(original)}
            />
          </div>
        )}
        <div className="action-icon uploaded-file-delete-icon">
          <FontAwesomeIcon icon={faXmark} onClick={() => remove(file)} />
        </div>
      </span>
      {!!children && <div className="uploaded-file-versions">{children}</div>}
    </div>
  );
}
