"use client";

import {
  faMinusSquare,
  faPlusSquare,
} from "@fortawesome/free-regular-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  ExpansionState,
  useFocus,
  useFocusDispatch,
} from "../../lib/context/FocusContext";
import { useSelectionDispatch } from "../../lib/context/SelectionContext";
import { Report } from "../../lib/types";
import { ReportItem } from "./ReportItem";

interface ReportsGroupProps {
  section: string;
  id: string;
  name: string;
  reports: Report[];
}

// TODO: organise by filename (collapsible)
export function ReportsGroup({
  section,
  id,
  name,
  reports,
}: ReportsGroupProps) {
  const { "report-group": groupFocus } = useFocus();
  const focusDispatch = useFocusDispatch();
  const selectionDispatch = useSelectionDispatch();

  const groupKey = `${section}-${id}`;
  const expanded = !groupFocus.expansions[groupKey];

  return (
    <div className="reports-group">
      <div
        className="reports-group-header"
        data-testid={`reports-group-${id}-header`}
        onClick={() => {
          selectionDispatch({ scroll: "none" });
          focusDispatch({
            type: "focus",
            target: "report-group",
            focus: {
              key: groupKey,
              value: expanded
                ? ExpansionState.Collapsed
                : ExpansionState.Expanded,
            },
          });
        }}
      >
        <FontAwesomeIcon
          className="reports-group-toggle"
          icon={expanded ? faMinusSquare : faPlusSquare}
        />
        <span className="reports-group-title">{name}</span>
        <span className="reports-group-count">({reports.length})</span>
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
