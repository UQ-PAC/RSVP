"use client";

import { faExclamation, faTrash } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { useRef, useState } from "react";
import {
  useAnalysisGroup,
  useAnalysisGroupDispatch,
} from "../../lib/context/AnalysisGroupContext";
import {
  ExpansionStatus,
  useExpansionDispatch,
} from "../../lib/context/ExpansionContext";
import { useSelectionDispatch } from "../../lib/context/SelectionContext";
import { useEventListener } from "../../lib/events";
import { remove, upload } from "../../lib/requests";
import { VerificationFile, VersionedFile } from "../../lib/types";
import { checkAnalysisGroup, getFileType } from "../../lib/util";
import { FileInput } from "./FileInput";
import { HiddenFileInput } from "./HiddenFileInput";
import { UploadedFile } from "./UploadedFile";

interface AnalysisGroupProps {
  removeGroup: () => void;
}

export function AnalysisGroup({ removeGroup }: AnalysisGroupProps) {
  const versionOriginFile = useRef<VersionedFile>(null);
  const versionInputRef = useRef<HTMLInputElement>(null);

  const { name, files } = useAnalysisGroup();
  const dispatch = useAnalysisGroupDispatch();

  const expansion = useExpansionDispatch();
  const selection = useSelectionDispatch();

  const registerComparisonFocus = (
    group: string,
    original: string,
    updated: string,
  ) =>
    expansion({
      type: "register",
      group: "source-file",
      id: `${original}${updated}`,
      conflict: group,
      kind: "c", // File comparison
      status: ExpansionStatus.Collapsed,
    });

  const deregisterComparisonFocus = (original: string, updated: string) =>
    expansion({
      type: "deregister",
      group: "source-file",
      id: `${original}${updated}`,
      kind: "c",
    });

  // Track whether verify was requested, to display error messages if relevant
  const [verifyRequested, setVerifyRequested] = useState(false);
  useEventListener("verificationRequested", () => setVerifyRequested(true));
  useEventListener("verificationComplete", () => setVerifyRequested(false));

  const addFiles = (toAdd: File[], existing?: VersionedFile) => {
    const uploaded = toAdd.map((file) => ({
      file,
      filename: file.name,
      filetype: getFileType(file),
      resolved: upload(file),
    }));

    // Add new files to group
    uploaded.forEach((file) =>
      dispatch({ type: "add", file, original: existing?.original }),
    );

    // Register source files as focus targets based on server ID,
    // and set focus to latest file
    Promise.all(uploaded.map((file) => file.resolved)).then((resolved) => {
      const ids = resolved.map((file) => file.serverId);

      ids.forEach((id, i) => {
        // New files are file versions, so need to add comparisons as focus targets
        if (existing) {
          existing.original.resolved
            .then(({ serverId }) => serverId)
            .then((originalId) => {
              // Add comparison to original
              registerComparisonFocus(originalId, originalId, id);

              // Add comparisons to existing versions
              existing.versions.forEach((version) =>
                version.resolved.then((uploadedVersion) =>
                  registerComparisonFocus(
                    originalId,
                    uploadedVersion.serverId,
                    id,
                  ),
                ),
              );

              // Add comparisons between other files added at the same time
              ids.slice(0, i).forEach((olderId) => {
                registerComparisonFocus(originalId, olderId, id);
              });

              // Register file as grouped focus target, if this is the latest version,
              // then focus on the file
              expansion({
                type: "register",
                group: "source-file",
                id,
                conflict: originalId,
                status:
                  i === ids.length - 1
                    ? ExpansionStatus.Expanded
                    : ExpansionStatus.Collapsed,
              });
            });
        } else {
          // Register file as standalone focus target
          expansion({
            type: "register",
            group: "source-file",
            id,
            conflict: id,
            status: ExpansionStatus.Expanded,
          });
        }
      });
    });
  };

  // Handle file version input selection
  const handleVersionFileInput = (e) => {
    if (!!e.target.files?.length && versionOriginFile.current) {
      const selectedFiles = Array.from<File>(e.target.files);
      addFiles(selectedFiles, versionOriginFile.current);
    }
    versionOriginFile.current = null;
  };

  // Programmatically open file selection dialog
  const openCedarFileDialog = (file: VersionedFile) => {
    versionOriginFile.current = file;
    versionInputRef.current?.click();
  };

  const { hasPolicy, hasSchema, hasEntities, error } =
    checkAnalysisGroup(files);

  const removeFile = (file: VerificationFile, original?: VersionedFile) => {
    file.resolved
      .then(({ serverId }) => serverId)
      .then((id) => {
        remove(id);

        expansion({
          type: "deregister",
          group: "source-file",
          id,
        });

        if (original) {
          // Delete comparison with original
          original.original.resolved.then(({ serverId }) =>
            deregisterComparisonFocus(serverId, id),
          );

          Promise.all(
            original.versions.map((version) => version.resolved),
          ).then((versions) => {
            const deleteIdx = versions.findIndex(
              ({ serverId }) => serverId === id,
            );

            // Delete comparisons with earlier versions
            versions
              .slice(0, deleteIdx)
              .forEach(({ serverId }) =>
                deregisterComparisonFocus(serverId, id),
              );

            // Delete comparisons with later versions
            versions
              .slice(deleteIdx + 1)
              .forEach(({ serverId }) =>
                deregisterComparisonFocus(id, serverId),
              );
          });
        }
      });

    dispatch({ type: "remove", file, original: original?.original });
  };

  return (
    <div className="analysis-group">
      <span
        className="analysis-group-header"
        onClick={() =>
          selection({
            scroll: "group",
            group: name,
          })
        }
      >
        <h4 className="analysis-group-title">{name}</h4>
        <div
          className="analysis-group-delete-icon action-icon"
          onClick={(e) => {
            e.stopPropagation();
            files.forEach((file) => {
              file.original.resolved
                .then(({ serverId }) => serverId)
                .then((id) => {
                  expansion({
                    type: "deregister",
                    group: "source-file",
                    id,
                  });
                });
            });
            removeGroup();
          }}
        >
          <FontAwesomeIcon icon={faTrash} />
        </div>
      </span>
      {files.length > 0 && (
        <div className="analysis-group-filelist">
          {files.map((file, i) => (
            <UploadedFile
              key={i}
              icon
              file={file.original}
              remove={removeFile}
              addChild={
                file.original.filetype === "cedar"
                  ? openCedarFileDialog
                  : undefined
              }
              original={file}
              version={file.versions.length ? 1 : undefined}
            >
              {file.versions.map((version, j) => (
                <UploadedFile
                  key={j}
                  version={j + 2}
                  file={version}
                  remove={(version) => removeFile(version, file)}
                />
              ))}
            </UploadedFile>
          ))}
        </div>
      )}
      <FileInput addFiles={addFiles} error={verifyRequested && error} />
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
      <HiddenFileInput
        ref={versionInputRef}
        accept=".cedar"
        handleFileInput={handleVersionFileInput}
      />
    </div>
  );
}
