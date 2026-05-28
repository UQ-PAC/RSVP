"use client";

import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import cx from "classnames";
import { useEffect, useRef, useState } from "react";
import {
  ExpansionStatus,
  useExpansion,
  useExpansionDispatch,
} from "../../lib/context/ExpansionContext";
import {
  useSelection,
  useSelectionDispatch,
} from "../../lib/context/SelectionContext";
import { getExpandIcon, getFileIcon } from "../../lib/fa-util";
import { Report, UploadedFile, VerificationFile } from "../../lib/types";
import { CodeRender } from "./CodeRender";
import { DiffRender } from "./DiffRender";
import { SourceVersionSelect } from "./SourceVersionSelect";

export type ResolvedFile = {
  file: VerificationFile;
  resolved: UploadedFile;
};

interface VersionedSourceFileParams {
  files: ResolvedFile[];
  reports: Report[];
  setExpansionCallback: (toggle: (status: ExpansionStatus) => void) => void;
}

export function VersionedSourceFile({
  files,
  reports,
  setExpansionCallback,
}: VersionedSourceFileParams) {
  const {
    "source-file": { expansions, conflicts: groups },
  } = useExpansion();
  const expansionDispatch = useExpansionDispatch();
  const { scroll, file: selectedFileId } = useSelection();
  const selectionDispatch = useSelectionDispatch();

  // Cache focused file to ensure correct version is expanded
  const focus = useRef<string>(null);
  const [selectedVersions, setSelectedVersions] = useState<{
    original?: string;
    updated?: string;
  }>();

  // Element reference for managing scrolling
  const file = useRef<HTMLDivElement>(null);

  const getFocusedFiles = (): {
    original?: ResolvedFile;
    updated?: ResolvedFile;
  } => {
    const focused = files.length
      ? groups?.[files[0].resolved.serverId]?.find(
          (id) => expansions[id] === ExpansionStatus.Expanded,
        )
      : undefined;

    if (focused) {
      const single = files.find(
        ({ resolved: { serverId } }) => serverId === focused,
      );

      if (single) {
        // A single file is selected
        return { original: single };
      } else {
        // A file comparison is selected
        for (let i = 0; i < files.length - 1; i++) {
          const current = files[i];

          const match = files
            .slice(i)
            .find(
              ({ resolved: { serverId: updatedId } }) =>
                `c/${current.resolved.serverId}${updatedId}` === focused,
            );

          if (match) {
            return { original: current, updated: match };
          }
        }
      }
    }
    return {};
  };

  // Find focused file
  const focused = files.length
    ? groups?.[files[0].resolved.serverId]?.find(
        (id) => expansions[id] === ExpansionStatus.Expanded,
      )
    : undefined;

  // If a file is focused, expand the file
  const expanded = !!focused;

  // Scroll selected file into view
  useEffect(() => {
    if (file.current && scroll === "file" && focused === selectedFileId) {
      file.current.scrollIntoView({
        block: "start",
        inline: "start",
        behavior: "smooth",
      });
    }
  }, [scroll, focused, selectedFileId]);

  // Change icon on hover, as opposed to style
  const [toggleHover, setToggleHover] = useState(false);

  const { original, updated } = getFocusedFiles();

  // Display icon based on file type
  const fileIcon = getFileIcon(
    original?.file.filetype ?? files[0].file.filetype,
  );

  // Expand or collapse file
  const toggleExpansion = (status?: ExpansionStatus) => {
    selectionDispatch({ scroll: "none" });

    if (focused) {
      if (status === ExpansionStatus.Expanded) {
        // Ignore if requesting expansion when already expanded
        return;
      }

      // Cache focus information so we can expand to the same file later
      focus.current = focused;
      setSelectedVersions({
        original: original?.resolved.serverId,
        updated: updated?.resolved.serverId,
      });
      expansionDispatch({
        type: "toggle",
        group: "source-file",
        id: focused,
        status: ExpansionStatus.Collapsed,
      });
    } else if (focus.current) {
      if (status === ExpansionStatus.Collapsed) {
        // Ignore if requesting collapse when already collapsed
        return;
      }

      // Clear focus cache
      setSelectedVersions({
        original: undefined,
        updated: undefined,
      });
      expansionDispatch({
        type: "toggle",
        group: "source-file",
        id: focus.current,
        status: ExpansionStatus.Expanded,
      });
    } else {
      console.error("Could not identify focus target");
    }
  };

  useEffect(() => {
    setExpansionCallback((status: ExpansionStatus) => toggleExpansion(status));
  });

  return (
    <div
      ref={file}
      className={cx("source-file", expanded ? "expanded" : "collapsed")}
    >
      <div className="source-file-header">
        <SourceVersionSelect
          versions={files.map(
            ({ file: { filename }, resolved: { serverId } }) => ({
              id: serverId,
              name: filename,
            }),
          )}
          selected={{
            original:
              original?.resolved.serverId ??
              selectedVersions?.original ??
              files[0].resolved.serverId,
            updated: updated?.resolved.serverId ?? selectedVersions?.updated,
          }}
          tabIcon={fileIcon}
          setVersion={(original, updated) => {
            if (original) {
              expansionDispatch({
                type: "toggle",
                group: "source-file",
                id: updated ? `${original}${updated}` : original,
                kind: updated ? "c" : undefined,
                status: ExpansionStatus.Expanded,
              });
            }
          }}
          expanded={expanded}
          onClickHeader={toggleExpansion}
          onMouseOverHeader={() => setToggleHover(true)}
          onMouseOutHeader={() => setToggleHover(false)}
        >
          <FontAwesomeIcon
            className="source-file-toggle"
            icon={getExpandIcon(toggleHover, expanded)}
          />
        </SourceVersionSelect>
      </div>
      {!!original && expanded && (
        <div className="source-file-content">
          {updated ? (
            <DiffRender
              original={original.file}
              updated={updated.file}
              originalId={original.resolved.serverId}
              updatedId={updated.resolved.serverId}
            />
          ) : (
            <CodeRender
              file={original.file}
              content={original.resolved.content}
              reports={reports.filter((report) =>
                report.sourceLocations.some(
                  (loc) => loc.location.source === original.file,
                ),
              )}
            />
          )}
        </div>
      )}
    </div>
  );
}
