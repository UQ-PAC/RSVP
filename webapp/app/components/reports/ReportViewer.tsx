"use client";

import { Report } from "../../types";
import { ReportsSection } from "./ReportsSection";

interface ReportViewerProps {
  reports?: Report[];
}

export function ReportViewer({ reports }: ReportViewerProps) {
  const sortByFile = (reports?: Report[]): [string, Report[]][] =>
    !reports
      ? []
      : Object.entries(
          reports.reduce(
            (
              sorted: { [filename: string]: Report[] },
              report: Report,
            ): { [filename: string]: Report[] } => {
              const source = report.primarySourceLocation.source?.filename;
              console.log(`source: ${source}`);
              if (source) {
                if (!sorted[source]) {
                  sorted[source] = [];
                }

                sorted[source].push(report);
              }
              return sorted;
            },
            {},
          ),
        );

  const info = sortByFile(
    reports?.filter((report) => report.severity === "info"),
  );
  const warn = sortByFile(
    reports?.filter((report) => report.severity === "warn"),
  );
  const err = sortByFile(
    reports?.filter((report) => report.severity === "err"),
  );

  return (
    <div className="reports-container">
      {!reports && (
        <p className="reports-instruction reports-not-run">
          Run verification to see reports
        </p>
      )}
      {reports && !reports.length && (
        <p className="reports-instruction no-reports">No reports to display</p>
      )}
      {!!err?.length && (
        <ReportsSection title="Errors" severity="err" reports={err} />
      )}
      {!!warn?.length && (
        <ReportsSection title="Warnings" severity="warn" reports={warn} />
      )}
      {!!info?.length && (
        <ReportsSection title="Information" severity="info" reports={info} />
      )}
    </div>
  );
}
