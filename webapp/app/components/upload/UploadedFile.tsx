import { faCodeCompare, faXmark } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { JSX } from "react";
import {
  ExpansionState,
  useFocusDispatch,
} from "../../lib/context/FocusContext";
import { useSelectionDispatch } from "../../lib/context/SelectionContext";
import { VerificationFile } from "../../lib/types";

interface UploadedFileProps {
  file: VerificationFile;
  remove: (file: VerificationFile) => void;
  children?: JSX.Element | JSX.Element[];
  addChild?: (file: VerificationFile) => void;
}

export function UploadedFile({
  file,
  children,
  remove,
  addChild,
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
                scroll: "source-file",
                file: serverId,
              });
            });
          }}
        >
          {file.filename}
        </span>
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
      {!!children && <div className="uploaded-file-versions">{children}</div>}
    </div>
  );
}
