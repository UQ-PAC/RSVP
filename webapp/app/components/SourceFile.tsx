"use client";

import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { useState } from "react";

import { Report } from "../SelectionContext";
import { faCaretDown, faCaretUp } from "@fortawesome/free-solid-svg-icons";
import { faFileLines } from "@fortawesome/free-regular-svg-icons/faFileLines";
import { CodeRender } from "./CodeRender";

interface SourceFileParams {
  filename: string;
  content: string;
  reports: Report[];
  openReportsDrawer: () => void;
}

export function SourceFile({
  filename,
  content,
  reports,
  openReportsDrawer,
}: SourceFileParams) {
  const [expanded, setExpanded] = useState(true);

  return (
    <div className="source-file">
      <div
        className="source-file-header"
        onClick={() => setExpanded(!expanded)}
      >
        <FontAwesomeIcon className="source-file-icon" icon={faFileLines} />
        <h2 className="source-file-name">{filename}</h2>
        <FontAwesomeIcon
          className="source-file-toggle"
          icon={expanded ? faCaretUp : faCaretDown}
        />
      </div>
      {expanded && (
        <CodeRender
          content={content}
          reports={reports}
          openReportsDrawer={openReportsDrawer}
        />
      )}
    </div>
  );
}
