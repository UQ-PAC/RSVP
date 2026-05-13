"use client";

import {
  ExpansionState,
  useFocusDispatch,
} from "../../lib/context/FocusContext";
import { FileType, Report, ReportSeverity } from "../../lib/types";
import { getFileIcon } from "../../lib/util";
import { ToggleAll } from "../shared/ToggleAll";
import { ReportsGroup } from "./ReportsGroup";

export type ReportGroup = [
  string,
  { filename: string; filetype?: FileType; reports: Report[] },
];

interface ReportsSectionProps {
  title: string;
  severity: ReportSeverity;
  reports: ReportGroup[];
}

export function ReportsSection({
  title,
  severity,
  reports,
}: ReportsSectionProps) {
  const focusDispatch = useFocusDispatch();

  const toggleAll = (expand: ExpansionState) => {
    reports.forEach(([group]) => {
      focusDispatch({
        type: "focus",
        target: "report-group",
        focus: { key: `${severity}-${group}`, value: expand },
      });
    });
  };

  return (
    <div className={`reports-section reports-section-${severity}`}>
      <span className="reports-section-header">
        <h4 className="reports-section-title">{title}</h4>
        <span className="reports-section-count">
          ({reports.flatMap((report) => report[1].reports).length})
        </span>
        <ToggleAll name="reports-section" toggle={toggleAll} />
      </span>

      {reports.map(([group, { filename, filetype, reports }]) => (
        <ReportsGroup
          key={`${severity}-${group}`}
          section={severity}
          name={filename}
          id={group}
          reports={reports}
          icon={filetype ? getFileIcon(filetype) : undefined}
        />
      ))}
    </div>
  );
}
