"use client";

import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";

import { FileSyntax, FileType, Report } from "../../types";
import { faFileLines } from "@fortawesome/free-regular-svg-icons/faFileLines";
import { CodeRender } from "./CodeRender";
import {
  faSquareMinus,
  faSquarePlus,
} from "@fortawesome/free-regular-svg-icons";
import {
  ExpansionState,
  useFocus,
  useFocusDispatch,
} from "../providers/FocusContext";
import { useSelectionDispatch } from "../providers/SelectionContext";

interface SourceFileParams {
  filename: string;
  filetype: FileType;
  content: string;
  reports: Report[];
}

export function SourceFile({
  filename,
  filetype,
  content,
  reports,
}: SourceFileParams) {
  const { "source-file": focus } = useFocus();
  const focusDispatch = useFocusDispatch();
  const selectionDispatch = useSelectionDispatch();

  const expanded = !focus[filename];

  let syntax: FileSyntax | undefined = undefined;

  switch (filetype) {
    case "cedar":
    case "cedarschema":
      syntax = "cedar";
      break;
    case "entities":
      syntax = "json";
      break;
    case "invariant":
      syntax = "invariant";
  }

  return (
    <div className={`source-file ${expanded ? "expanded" : "collapsed"}`}>
      <div
        className="source-file-header"
        onClick={() => {
          selectionDispatch({ type: "other", scroll: "none" });
          focusDispatch({
            type: "source-file",
            key: filename,
            value: expanded
              ? ExpansionState.Collapsed
              : ExpansionState.Expanded,
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
      {expanded && (
        <CodeRender content={content} syntax={syntax} reports={reports} />
      )}
    </div>
  );
}
