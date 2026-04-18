"use client";

import { Report, ReportSeverity } from "../../types";
import { ExpansionState, useFocusDispatch } from "../providers/FocusContext";
import { ToggleAll } from "../shared/ToggleAll";
import { ReportsGroup } from "./ReportsGroup";

interface ReportsSectionProps {
  title: string;
  severity: ReportSeverity;
  reports: [string, Report[]][];
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
        type: "report-group",
        key: `${severity}-${group}`,
        value: expand,
      });
    });
  };

  return (
    <div className={`reports-section reports-section-${severity}`}>
      <span className="reports-section-header">
        <h4 className="reports-section-title">{title}</h4>
        <ToggleAll name="reports-section" toggle={toggleAll} />
      </span>

      {reports.map(([group, items]) => (
        <ReportsGroup
          key={`${severity}-${group}`}
          section={severity}
          name={group}
          reports={items}
        />
      ))}
    </div>
  );
}
