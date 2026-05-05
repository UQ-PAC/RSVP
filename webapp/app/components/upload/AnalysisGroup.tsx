"use client";

import { faTrashCan } from "@fortawesome/free-regular-svg-icons";
import { faExclamation } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import cx from "classnames";
import { useRef } from "react";
import {
  useAnalysisGroup,
  useAnalysisGroupDispatch,
} from "../../lib/context/AnalysisGroupContext";
import {
  ExpansionState,
  useFocusDispatch,
} from "../../lib/context/FocusContext";
import { remove, upload } from "../../lib/requests";
import { VerificationFile } from "../../lib/types";
import { checkAnalysisGroup, getFileType } from "../../lib/util";
import { FileInput } from "./FileInput";
import { HiddenFileInput } from "./HiddenFileInput";
import { UploadedFile } from "./UploadedFile";

interface AnalysisGroupProps {
  removeGroup: () => void;
}

export function AnalysisGroup({ removeGroup }: AnalysisGroupProps) {
  const versionOriginFile = useRef<VerificationFile>(null);
  const versionInputRef = useRef<HTMLInputElement>(null);

  const { name, files, verifyRequested } = useAnalysisGroup();
  const dispatch = useAnalysisGroupDispatch();

  const focus = useFocusDispatch();

  const addFiles = (toAdd: File[], original?: VerificationFile) =>
    toAdd.forEach((file) => {
      dispatch({
        type: "add",
        file: {
          file,
          filename: file.name,
          filetype: getFileType(file),
          resolved: upload(file).then((uploaded) => {
            if (!original) {
              // Expand added file if completely new file (not a version)
              focus({
                type: "focus",
                target: "source-file",
                focus: {
                  key: uploaded.serverId,
                  value: ExpansionState.Expanded,
                },
              });
            }
            return uploaded;
          }),
        },
        original,
      });
    });

  // Handle regular file input selection
  const handleVersionFileInput = (e) => {
    if (
      e.target.files &&
      e.target.files.length > 0 &&
      versionOriginFile.current
    ) {
      const selectedFiles = Array.from<File>(e.target.files);
      addFiles(selectedFiles, versionOriginFile.current);
    }
    versionOriginFile.current = null;
  };

  // Programmatically open file selection dialog
  const openCedarFileDialog = (file: VerificationFile) => {
    versionOriginFile.current = file;
    versionInputRef.current?.click();
  };

  const { hasPolicy, hasSchema, hasEntities, error } =
    checkAnalysisGroup(files);

  const removeFile = (file: VerificationFile, original?: VerificationFile) => {
    file.resolved.then((uploaded) => {
      remove(uploaded.serverId);

      if (!original) {
        // File is original, de-register focus group
        focus({
          type: "deregister",
          target: "source-file",
          deregister: uploaded.serverId,
        });
      }
    });
    dispatch({ type: "remove", file, original });
  };

  return (
    <div className="analysis-group">
      <span className="analysis-group-header">
        <h4
          className={cx(
            "analysis-group-title",
            error && verifyRequested && "error",
          )}
        >
          {name}
        </h4>
        <FontAwesomeIcon
          className="analysis-group-delete-icon"
          icon={faTrashCan}
          onClick={removeGroup}
        />
      </span>
      <div className="analysis-group-filelist">
        {files.map((file, i) => (
          <UploadedFile
            key={i}
            file={file.original}
            remove={removeFile}
            addChild={
              file.original.filetype === "cedar"
                ? openCedarFileDialog
                : undefined
            }
          >
            {file.versions.map((version, j) => (
              <UploadedFile
                key={j}
                file={version}
                remove={(version) => removeFile(version, file.original)}
              />
            ))}
          </UploadedFile>
        ))}
      </div>
      {verifyRequested && error && (
        <div className="analysis-group-errors">
          {!hasPolicy && (
            <span className="analysis-group-error">
              <FontAwesomeIcon
                className="analysis-group-error-icon"
                icon={faExclamation}
              />
              At least one policy file is required
            </span>
          )}
          {!hasSchema && (
            <span className="analysis-group-error">
              <FontAwesomeIcon
                className="analysis-group-error-icon"
                icon={faExclamation}
              />
              At least one schema file is required
            </span>
          )}
          {!hasEntities && (
            <span className="analysis-group-error">
              <FontAwesomeIcon
                className="analysis-group-error-icon"
                icon={faExclamation}
              />
              At least one entities file is required
            </span>
          )}
        </div>
      )}
      <FileInput addFiles={addFiles} />
      <HiddenFileInput
        ref={versionInputRef}
        accept=".cedar"
        handleFileInput={handleVersionFileInput}
      />
    </div>
  );
}
