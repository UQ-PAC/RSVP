"use client";

import { SourceFile } from "./SourceFile";
import { Report, VerificationFile } from "../../types";
import { useVerification } from "../providers/VerificationContext";
import { getFileType } from "@/app/util";
import { Fallback } from "./Fallback";

import "./sources.css";

export function SourceFileViewer() {
  const verificationContext = useVerification();

  const filterReports = (
    source: VerificationFile,
    reports?: Promise<Report[]>,
  ): Promise<Report[]> =>
    reports?.then((reports) =>
      reports.filter(
        // TODO: multiple locations
        (report) => report.primarySourceLocation.source === source,
      ),
    ) ?? Promise.resolve([]);

  const groups = Object.entries(verificationContext);

  return (
    <div className="source-files-container">
      {groups.map(([name, group], i) => (
        <div key={i} className="source-files-analysis-group">
          <span className="source-files-analysis-group-header">
            <h2 className="source-files-analysis-group-title">{name}</h2>
          </span>
          {group.files.map(({ original: source }, i) => (
            <SourceFile
              key={i}
              filename={source.file.name}
              filetype={getFileType(source.file)}
              content={source.resolved.then((uploaded) => uploaded.content)}
              reports={filterReports(source, group.reports)}
            />
          ))}
          {!group.files.length && (
            <Fallback instruction="Upload Cedar policy and schema files" />
          )}
        </div>
      ))}
      {!groups.length && <Fallback instruction="Create a policy set" />}
    </div>
  );
}
