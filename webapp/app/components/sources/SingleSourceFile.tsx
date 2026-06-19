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

interface SingleSourceFileParams {
  file: VerificationFile;
  uploaded: UploadedFile;
  reports: Report[];
  setExpansionCallback: (toggle: (status: ExpansionStatus) => void) => void;
}

export function SingleSourceFile({
  file,
  uploaded,
  reports,
  setExpansionCallback,
}: SingleSourceFileParams) {
  const {
    "source-file": { expansions },
  } = useExpansion();
  const expansionDispatch = useExpansionDispatch();
  const { scroll, file: selectedFileId } = useSelection();
  const selectionDispatch = useSelectionDispatch();

  // Set callback so file can be expanded/collapsed using EXPAND ALL/COLLAPSE ALL
  file.resolved.then(({ serverId }) =>
    setExpansionCallback((status: ExpansionStatus) =>
      expansionDispatch({
        type: "toggle",
        group: "source-file",
        id: serverId,
        status,
      }),
    ),
  );

  // Element reference for managing scrolling
  const container = useRef<HTMLDivElement>(null);

  // If a file is focused, expand the file
  const expanded = expansions[uploaded.serverId] === ExpansionStatus.Expanded;

  // Scroll selected policy into view
  useEffect(() => {
    if (
      container.current &&
      scroll === "file" &&
      uploaded.serverId === selectedFileId
    ) {
      container.current.scrollIntoView({
        block: "start",
        inline: "start",
        behavior: "smooth",
      });
    }
  }, [scroll, uploaded.serverId, selectedFileId]);

  // Change icon on hover, as opposed to style
  const [toggleHover, setToggleHover] = useState(false);

  // Expand or collapse file
  const toggleExpansion = () => {
    selectionDispatch({ scroll: "none" });
    file.resolved.then(({ serverId }) =>
      expansionDispatch({
        type: "toggle",
        group: "source-file",
        id: serverId,
        status: expanded ? ExpansionStatus.Collapsed : ExpansionStatus.Expanded,
      }),
    );
  };

  return (
    <div
      ref={container}
      className={cx("source-file", expanded ? "expanded" : "collapsed")}
    >
      <div
        className="source-file-header"
        onClick={toggleExpansion}
        onMouseOver={() => setToggleHover(true)}
        onMouseOut={() => setToggleHover(false)}
      >
        <FontAwesomeIcon
          className="source-file-icon"
          icon={getFileIcon(file.filetype)}
        />
        <h2 className="source-file-name">{file.filename}</h2>
        <FontAwesomeIcon
          className="source-file-toggle"
          icon={getExpandIcon(toggleHover, expanded)}
        />
      </div>
      {expanded && (
        <div className="source-file-content">
          <CodeRender
            file={file}
            content={uploaded.content}
            reports={reports.filter((report) =>
              report.sourceLocations.some(
                (loc) => loc.location.file === uploaded.serverId,
              ),
            )}
          />
        </div>
      )}
    </div>
  );
}
