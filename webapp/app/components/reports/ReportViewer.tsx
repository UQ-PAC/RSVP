"use client";

import { useEffect, useState } from "react";
import { Report, ReportSeverity } from "../../types";
import { useVerification } from "../providers/VerificationContext";
import { ReportsSection } from "./ReportsSection";

import { faSpinner } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import "./reports.css";

type GroupedReports = [string, Report[]][];

export function ReportViewer() {
  const [info, setInfo] = useState<GroupedReports>([]);
  const [warn, setWarn] = useState<GroupedReports>([]);
  const [err, setErr] = useState<GroupedReports>([]);

  const [progress, setProgress] = useState(false);
  const [verified, setVerified] = useState(false);

  const verificationContext = useVerification();

  // TODO: sort by group?
  useEffect(() => {
    const unresolved: Promise<Report[]>[] = Object.values(verificationContext)
      .filter(({ reports }) => !!reports)
      .map(({ reports }) => reports as Promise<Report[]>);

    if (unresolved.length) {
      setProgress(true);
      setInfo([]);
      setWarn([]);
      setErr([]);
    }

    Promise.all(unresolved).then((resolved) => {
      const reports = resolved?.flat();
      setProgress(false);

      if (reports && reports.length) {
        setVerified(true);

        setInfo(sortByFile("info", reports));
        setWarn(sortByFile("warn", reports));
        setErr(sortByFile("err", reports));
      }
    });
  }, [verificationContext]);

  const count = info.length + warn.length + err.length;

  return (
    <div className="reports-container">
      {!progress && !count && (
        <p className="reports-instruction no-reports">No reports to display</p>
      )}
      {progress && (
        <FontAwesomeIcon
          className="verification-progress-icon"
          icon={faSpinner}
        />
      )}
      {progress && (
        <p className="reports-instruction verifying">Verifying policies...</p>
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
