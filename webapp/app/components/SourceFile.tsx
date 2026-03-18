"use client";

import cx from "classnames";

import { faAngleDown, faAngleUp } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { useState } from "react";

interface SourceLoc {
  file: string;
  offset: number;
  len: number;
}
export interface Report {
  id: string;
  source: SourceLoc;
  severity: "info" | "warn" | "err";
  message: string;
}
interface SourceFileParams {
  filename: string;
  content: string;
  reports: Report[];
  selected?: string;
  active?: string;
  onclick: (id: string) => void;
  onenter: (id: string) => void;
  onleave: (id: string) => void;
}

export function SourceFile({
  filename,
  content,
  reports,
  selected,
  active,
  onclick,
  onenter,
  onleave,
}: SourceFileParams) {
  const [expand, setExpand] = useState(true);

  const begin: string = content.slice(0, reports.at(0)?.source.offset);

  return (
    <div className="source-file-render">
      <div className="source-file-header" onClick={() => setExpand(!expand)}>
        <h2 className="source-file-name">{filename}</h2>
        {expand ? (
          <FontAwesomeIcon
            className="source-file-toggle source-file-collapse"
            icon={faAngleUp}
          />
        ) : (
          <FontAwesomeIcon
            className="source-file-toggle source-file-expand"
            icon={faAngleDown}
          />
        )}
      </div>
      {expand ? (
        <div className="source-file-contents">
          {begin}
          {reports.map((report: Report, index) => (
            <span key={report.id}>
              <span
                className={cx(
                  "source-report",
                  `source-report-${report.severity}`,
                  selected === report.id && active !== report.id && "selected",
                  active === report.id && "active",
                )}
                data-message={report.message}
                onClick={() => onclick(report.id)}
                onMouseEnter={() => onenter(report.id)}
                onMouseLeave={() => onleave(report.id)}
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
