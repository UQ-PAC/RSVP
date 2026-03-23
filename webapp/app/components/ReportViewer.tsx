"use client";

import { ReportItem } from "./ReportItem";
import { Report, useSelection } from "../SelectionContext";
import { useRef } from "react";

interface ReportViewerProps {
  reports?: Report[];
}

export function ReportViewer({ reports }: ReportViewerProps) {
  const info = reports?.filter((report) => report.severity === "info");
  const warn = reports?.filter((report) => report.severity === "warn");
  const err = reports?.filter((report) => report.severity === "err");

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
        <div className="reports-group reports-group-err">
          <h4 className="reports-group-title">Errors</h4>
          {err.map((report) => (
            <ReportItem key={report.id} report={report} />
          ))}
        </div>
      )}
      {!!warn?.length && (
        <div className="reports-group reports-group-warn">
          <h4 className="reports-group-title">Warnings</h4>
          {warn.map((report) => (
            <ReportItem key={report.id} report={report} />
          ))}
        </div>
      )}
      {!!info?.length && (
        <div className="reports-group reports-group-info">
          <h4 className="reports-group-title">Information</h4>
          {info.map((report) => (
            <ReportItem key={report.id} report={report} />
          ))}
        </div>
      )}
    </div>
  );
}
