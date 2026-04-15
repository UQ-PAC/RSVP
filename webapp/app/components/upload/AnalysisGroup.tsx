"use client";

import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { FileInput } from "./FileInput";
import { faTrashCan } from "@fortawesome/free-regular-svg-icons";
import {
  emptyVerificationGroup,
  useVerification,
  useVerificationDispatch,
} from "../providers/VerificationContext";
import { VerificationFile } from "@/app/types";
import { remove, upload } from "../../requests";
import { UploadedFile } from "./UploadedFile";
import { HiddenFileInput } from "./HiddenFileInput";
import { useRef } from "react";
import { getFileType } from "../../util";

interface AnalysisGroupProps {
  name: string;
  removeGroup: () => void;
}

export function AnalysisGroup({ name, removeGroup }: AnalysisGroupProps) {
  const versionOriginFile = useRef<VerificationFile>(null);
  const versionInputRef = useRef<HTMLInputElement>(null);

  const context = useVerification();
  const dispatch = useVerificationDispatch();

  const { files } = context[name] ?? emptyVerificationGroup;

  const addFiles = (toAdd: File[], original?: VerificationFile) =>
    toAdd.forEach((file) => {
      dispatch({
        type: "add",
        group: name,
        file: {
          file,
          filetype: getFileType(file),
          resolved: upload(file),
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

  const removeFile = (file: VerificationFile, original?: VerificationFile) => {
    file.resolved.then((uploaded) => remove(uploaded.serverId));
    dispatch({ type: "remove", group: name, file, original });
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
        {files.map((file, i) => (
          <UploadedFile
            key={i}
            group={name}
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
                group={name}
                file={version}
                remove={removeFile}
              />
            ))}
          </UploadedFile>
        ))}
      </div>
      <FileInput addFiles={addFiles} />
      <HiddenFileInput
        ref={versionInputRef}
        accept="cedar"
        handleFileInput={handleVersionFileInput}
      />
    </div>
  );
}
