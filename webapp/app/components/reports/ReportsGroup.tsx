"use client";

import { ReportItem } from "./ReportItem";
import { Report } from "../../types";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faMinusSquare,
  faPlusSquare,
} from "@fortawesome/free-regular-svg-icons";
import { useFocus, useFocusDispatch } from "../providers/FocusContext";
import { useSelectionDispatch } from "../providers/SelectionContext";

interface ReportsGroupProps {
  section: string;
  name: string;
  reports: Report[];
}

// TODO: organise by filename (collapsible)
export function ReportsGroup({ section, name, reports }: ReportsGroupProps) {
  const { "report-group": groupFocus } = useFocus();
  const focusDispatch = useFocusDispatch();
  const selectionDispatch = useSelectionDispatch();

  const groupKey = `${section}-${name}`;
  const expanded = !!groupFocus[groupKey];

  return (
    <div className="reports-group">
      <div
        className="reports-group-header"
        onClick={() => {
          selectionDispatch({ type: "other", scroll: "none" });
          focusDispatch({
            type: "report-group",
            key: groupKey,
            value: !expanded,
          });
        }}
      >
        <FontAwesomeIcon
          className="reports-group-toggle"
          icon={expanded ? faMinusSquare : faPlusSquare}
        />
        <span className="reports-group-title">{name}</span>
      </div>
      {expanded && (
        <div className="reports-group-content">
          {reports.map((report) => (
            <ReportItem key={report.id} report={report} />
          ))}
        </div>
      )}
    </div>
  );
}
