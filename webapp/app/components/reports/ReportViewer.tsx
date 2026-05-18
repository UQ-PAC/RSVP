"use client";

import { useEffect, useState } from "react";
import { useVerification } from "../../lib/context/VerificationContext";
import {
  FileType,
  Report,
  ReportSeverity,
  VerificationFile,
} from "../../lib/types";
import { ReportGroup, ReportsSection } from "./ReportsSection";

import { ProgressSpinner } from "../shared/ProgressSpinner";
import "./reports.css";

export function ReportViewer() {
  const [info, setInfo] = useState<ReportGroup[]>([]);
  const [warn, setWarn] = useState<ReportGroup[]>([]);
  const [err, setErr] = useState<ReportGroup[]>([]);

  const [progress, setProgress] = useState(false);

  const verificationContext = useVerification();

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

      if (reports) {
        sortByFile("info", reports).then((sorted) => setInfo(sorted));
        sortByFile("warn", reports).then((sorted) => setWarn(sorted));
        sortByFile("err", reports).then((sorted) => setErr(sorted));
      }
    });
  }, [verificationContext]);

  const count = info.length + warn.length + err.length;

  return (
    <div className="reports-container">
      {!progress && !count && (
        <p className="reports-instruction no-reports">No reports to display</p>
      )}
      {progress && <ProgressSpinner text="Verifying policies..." />}
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

  async function sortByFile(
    severity: ReportSeverity,
    reports?: Report[],
  ): Promise<ReportGroup[]> {
    if (!reports) return [];

    return Promise.all(
      reports
        .filter((report) => report.severity === severity)
        .map((report) => {
          if (
            report.sourceLocations.length &&
            report.sourceLocations[0].location.source
          ) {
            const primaryFile = report.sourceLocations[0].location
              .source as VerificationFile;
            return primaryFile.resolved.then((uploaded) => ({
              id: uploaded.serverId,
              filename: primaryFile.filename ?? primaryFile.file.name,
              filetype: primaryFile.filetype,
              report,
            }));
          } else {
            return Promise.resolve({
              id: "other",
              filename: "Other",
              report,
            });
          }
        }),
    ).then((resolved) =>
      Object.entries(
        resolved.reduce(
          (
            sorted: {
              [id: string]: {
                filename: string;
                filetype?: FileType;
                reports: Report[];
              };
            },
            current: {
              report: Report;
              id: string;
              filetype?: FileType;
              filename: string;
            },
          ): {
            [id: string]: {
              filename: string;
              filetype?: FileType;
              reports: Report[];
            };
          } => {
            if (!sorted[current.id]) {
              sorted[current.id] = {
                filename: current.filename,
                filetype: current.filetype,
                reports: [],
              };
            }

            sorted[current.id].reports.push(current.report);
            return sorted;
          },
          {},
        ),
      ),
    );
  }
}
