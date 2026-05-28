"use client";

import { useEffect, useRef, useState } from "react";
import {
  FileType,
  Report,
  ReportSeverity,
  VerificationFile,
} from "../../lib/types";
import {
  ReportGroupData,
  ReportGroupListing,
  ReportSection,
} from "./ReportSection";

import { useVerification } from "../../lib/context/VerificationContext";
import { useEventListener } from "../../lib/events";
import { ProgressSpinner } from "../shared/ProgressSpinner";
import "./reports.css";

export function ReportViewer() {
  const [info, setInfo] = useState<ReportGroupListing[]>([]);
  const [warn, setWarn] = useState<ReportGroupListing[]>([]);
  const [err, setErr] = useState<ReportGroupListing[]>([]);

  const verificationContext = useVerification();

  const [progress, setProgress] = useState(false);

  // Ensure state not updated during render of other components
  const pending = useRef(false);

  useEventListener("verificationPending", () => {
    pending.current = true;
  });

  useEffect(() => {
    if (pending.current) {
      setProgress(true);
      setInfo([]);
      setWarn([]);
      setErr([]);
      pending.current = false;
    }
  }, [pending.current]);

  useEventListener("verificationComplete", () => setProgress(false));

  useEffect(() => {
    const unresolved = Object.values(verificationContext)
      .filter(({ reports }) => !!reports)
      .map(({ reports }) => reports as Promise<Report[]>);

    if (!progress && unresolved.length) {
      Promise.all(unresolved).then((resolved) => {
        const reports = resolved.flat();

        sortByFile("info", reports).then((sorted) => setInfo(sorted));
        sortByFile("warn", reports).then((sorted) => setWarn(sorted));
        sortByFile("err", reports).then((sorted) => setErr(sorted));
      });
    }
  }, [verificationContext, progress]);

  const count = info.length + warn.length + err.length;

  return (
    <div className="reports-container">
      {!progress && !count && (
        <p className="reports-instruction no-reports">No reports to display</p>
      )}
      {progress && <ProgressSpinner text="Verifying policies..." />}
      {!!err.length && (
        <ReportSection title="Errors" severity="err" reports={err} />
      )}
      {!!warn.length && (
        <ReportSection title="Warnings" severity="warn" reports={warn} />
      )}
      {!!info.length && (
        <ReportSection title="Information" severity="info" reports={info} />
      )}
    </div>
  );
}

interface ReportGroupIR {
  report: Report;
  id: string;
  filetype?: FileType;
  filename: string;
}

interface ReportGroupDict {
  [id: string]: ReportGroupData;
}

async function sortByFile(
  severity: ReportSeverity,
  reports: Report[],
): Promise<ReportGroupListing[]> {
  return Promise.all(
    reports.filter((report) => report.severity === severity).map(getReportData),
  ).then((resolved) => Object.entries(resolved.reduce(groupByFile, {})));
}

async function getReportData(report: Report): Promise<ReportGroupIR> {
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
}

function groupByFile(
  sorted: ReportGroupDict,
  current: ReportGroupIR,
): ReportGroupDict {
  if (!sorted[current.id]) {
    sorted[current.id] = {
      filename: current.filename,
      filetype: current.filetype,
      reports: [],
    };
  }

  sorted[current.id].reports.push(current.report);
  return sorted;
}
