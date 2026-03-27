"use client";

import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";

import { Report } from "../../types";
import { faFileLines } from "@fortawesome/free-regular-svg-icons/faFileLines";
import { CodeRender } from "./CodeRender";
import {
  faSquareMinus,
  faSquarePlus,
} from "@fortawesome/free-regular-svg-icons";
import { useFocus, useFocusDispatch } from "../providers/FocusContext";
import { useSelectionDispatch } from "../providers/SelectionContext";

interface SourceFileParams {
  filename: string;
  content: string;
  reports: Report[];
}

export function SourceFile({ filename, content, reports }: SourceFileParams) {
  const { "source-file": focus } = useFocus();
  const focusDispatch = useFocusDispatch();
  const selectionDispatch = useSelectionDispatch();

  const expanded = !!focus[filename];

  return (
    <div className={`source-file ${expanded ? "expanded" : "collapsed"}`}>
      <div
        className="source-file-header"
        onClick={() => {
          selectionDispatch({ type: "other", scroll: "none" });
          focusDispatch({
            type: "source-file",
            key: filename,
            value: !expanded,
          });
        }}
      >
        <FontAwesomeIcon className="source-file-icon" icon={faFileLines} />
        <h2 className="source-file-name">{filename}</h2>
        <FontAwesomeIcon
          className="source-file-toggle"
          icon={expanded ? faSquareMinus : faSquarePlus}
        />
      </div>
      {expanded && <CodeRender content={content} reports={reports} />}
    </div>
  );
}
