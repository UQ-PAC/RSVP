"use client";

import {
  ExpansionState,
  useFocusDispatch,
} from "../../lib/context/FocusContext";
import { Report, ReportSeverity } from "../../lib/types";
import { getFileIcon, getFileType } from "../../lib/util";
import { ToggleAll } from "../shared/ToggleAll";
import { ReportsGroup } from "./ReportsGroup";

export type ReportGroup = [string, { filename: string; reports: Report[] }];

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

      {reports.map(([group, { filename, reports }]) => (
        <ReportsGroup
          key={`${severity}-${group}`}
          section={severity}
          name={filename}
          id={group}
          reports={reports}
          icon={
            group === "other" ? undefined : getFileIcon(getFileType(filename))
          }
        />
      ))}
    </div>
  );
}
