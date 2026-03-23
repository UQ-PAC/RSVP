"use client";

import cx from "classnames";

import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { useState } from "react";
import { Roboto_Mono } from "next/font/google";

import {
  Report,
  useSelection,
  useSelectionDispatch,
} from "../SelectionContext";
import { faCaretDown, faCaretUp } from "@fortawesome/free-solid-svg-icons";
import { faFileLines } from "@fortawesome/free-regular-svg-icons/faFileLines";

interface SourceFileParams {
  filename: string;
  content: string;
  reports: Report[];
}

const robotoMono = Roboto_Mono({
  subsets: ["latin"],
});

export function SourceFile({ filename, content, reports }: SourceFileParams) {
  const [expand, setExpand] = useState(true);
  const { selected, hovered } = useSelection();
  const dispatch = useSelectionDispatch();

  const begin: string = content.slice(0, reports.at(0)?.source.offset);

  return (
    <div className="source-file-render">
      <div className="source-file-header" onClick={() => setExpand(!expand)}>
        <FontAwesomeIcon className="source-file-icon" icon={faFileLines} />
        <h2 className="source-file-name">{filename}</h2>
        {expand ? (
          <FontAwesomeIcon
            className="source-file-toggle source-file-collapse"
            icon={faCaretUp}
          />
        ) : (
          <FontAwesomeIcon
            className="source-file-toggle source-file-expand"
            icon={faCaretDown}
          />
        )}
      </div>
      {expand ? (
        <div className={`source-file-contents ${robotoMono.className}`}>
          {begin}
          {reports.map((report: Report, index) => (
            <span key={report.id}>
              <span
                id={`source-report-${report.id}`}
                className={cx(
                  "source-report",
                  `source-report-${report.severity}`,
                  selected === report.id && hovered !== report.id && "selected",
                  hovered === report.id && "hovered",
                )}
                data-message={report.message}
                onClick={() =>
                  dispatch({ type: "click", id: report.id, source: "source" })
                }
                onMouseEnter={() =>
                  dispatch({
                    type: "mouseEnter",
                    id: report.id,
                    source: "source",
                  })
                }
                onMouseLeave={() =>
                  dispatch({
                    type: "mouseLeave",
                    id: report.id,
                    source: "source",
                  })
                }
              >
                {content.slice(
                  report.source.offset,
                  report.source.offset + report.source.len,
                )}
              </span>
              {content.slice(
                report.source.offset + report.source.len,
                index == reports.length - 1
                  ? content.length
                  : reports.at(index + 1)?.source.offset,
              )}
            </span>
          ))}
        </div>
      ) : (
        <></>
      )}
    </div>
  );
}
