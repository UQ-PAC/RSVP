"use client";

import { ReportsGroup } from "./ReportsGroup";
import { Report, ReportSeverity } from "../../types";
import { useFocusDispatch } from "../providers/FocusContext";

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

  const toggleAll = (expand: boolean) => {
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

        <span className="reports-section-toggle">
          <a className="reports-section-expand" onClick={() => toggleAll(true)}>
            EXPAND ALL
          </a>
          <span>|</span>
          <a
            className="reports-section-collapse"
            onClick={() => toggleAll(false)}
          >
            COLLAPSE ALL
          </a>
        </span>
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
