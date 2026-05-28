"use client";

import {
  ExpansionStatus,
  useExpansionDispatch,
} from "../../lib/context/ExpansionContext";
import { getFileIcon } from "../../lib/fa-util";
import { FileType, Report, ReportSeverity } from "../../lib/types";
import { ToggleAll } from "../shared/ToggleAll";
import { ReportGroup } from "./ReportGroup";

export type ReportGroupData = {
  filename: string;
  filetype?: FileType;
  reports: Report[];
};

export type ReportGroupListing = [string, ReportGroupData];

interface ReportSectionProps {
  title: string;
  severity: ReportSeverity;
  reports: ReportGroupListing[];
}

export function ReportSection({
  title,
  severity,
  reports,
}: ReportSectionProps) {
  const expansionDispatch = useExpansionDispatch();

  const toggleAll = (expand: ExpansionStatus) => {
    reports.forEach(([group]) => {
      expansionDispatch({
        type: "toggle",
        group: "report-group",
        id: `${severity}-${group}`,
        status: expand,
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
        <ReportGroup
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
