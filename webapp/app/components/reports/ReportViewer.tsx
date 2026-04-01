"use client";

import { useEffect, useState } from "react";
import { Report, ReportSeverity } from "../../types";
import { ReportsSection } from "./ReportsSection";
import { useVerification } from "../providers/VerificationContext";

type GroupedReports = [string, Report[]][];

export function ReportViewer() {
  // const [reports, setReports] = useState<Report[]>([]);
  const [info, setInfo] = useState<GroupedReports>([]);
  const [warn, setWarn] = useState<GroupedReports>([]);
  const [err, setErr] = useState<GroupedReports>([]);

  const verificationContext = useVerification();

  useEffect(() => {
    const unresolved: Promise<Report[]>[] = Object.values(verificationContext)
      .filter(({ reports }) => !!reports)
      .map(({ reports }) => reports as Promise<Report[]>);

    Promise.all(unresolved).then((resolved) => {
      const reports = resolved?.flat();

      if (reports) {
        setInfo(sortByFile("info", reports));
        setWarn(sortByFile("warn", reports));
        setErr(sortByFile("err", reports));
      }
    });
  }, [verificationContext]);

  const count = info.length + warn.length + err.length;

  return (
    <div className="reports-container">
      {!count && (
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

  function sortByFile(
    severity: ReportSeverity,
    reports?: Report[],
  ): [string, Report[]][] {
    if (!reports) return [];

    return Object.entries(
      reports
        .filter((report) => report.severity === severity)
        .reduce(
          (
            sorted: { [filename: string]: Report[] },
            report: Report,
          ): { [filename: string]: Report[] } => {
            const source = report.primarySourceLocation.source?.file.name;
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
  }
}
