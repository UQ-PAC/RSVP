import { faCodeCompare, faXmark } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { JSX } from "react";
import {
  ExpansionState,
  useFocusDispatch,
} from "../../lib/context/FocusContext";
import { useSelectionDispatch } from "../../lib/context/SelectionContext";
import { VerificationFile } from "../../lib/types";
import { getFileIcon } from "../../lib/util";

interface UploadedFileProps {
  file: VerificationFile;
  remove: (file: VerificationFile) => void;
  children?: JSX.Element | JSX.Element[];
  addChild?: (file: VerificationFile) => void;
  icon?: boolean;
  version?: number;
}

export function UploadedFile({
  file,
  children,
  remove,
  addChild,
  icon = false,
  version,
}: UploadedFileProps) {
  const focusDispatch = useFocusDispatch();
  const selectionDispatch = useSelectionDispatch();

  return (
    <div className="uploaded-file-container">
      <span className="uploaded-file">
        <span
          className="uploaded-file-name"
          onClick={() => {
            file.resolved.then(({ serverId }) => {
              focusDispatch({
                type: "focus",
                target: "source-file",
                focus: { key: serverId, value: ExpansionState.Expanded },
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
        {addChild && (
          <div className="action-icon uploaded-file-version-icon">
            <FontAwesomeIcon
              icon={faCodeCompare}
              onClick={() => addChild(file)}
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
