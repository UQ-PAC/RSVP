"use client";

import { IconDefinition } from "@fortawesome/free-regular-svg-icons";

import {
  faSquareMinus as regularMinus,
  faSquarePlus as regularPlus,
} from "@fortawesome/free-regular-svg-icons";
import {
  faSquareMinus as solidMinus,
  faSquarePlus as solidPlus,
} from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { useState } from "react";
import {
  ExpansionStatus,
  useExpansion,
  useExpansionDispatch,
} from "../../lib/context/ExpansionContext";
import { useSelectionDispatch } from "../../lib/context/SelectionContext";
import { Report } from "../../lib/types";
import { ReportItem } from "./ReportItem";

interface ReportGroupProps {
  section: string;
  id: string;
  name: string;
  reports: Report[];
  icon?: IconDefinition;
}

export function ReportGroup({
  section,
  id,
  name,
  reports,
  icon,
}: ReportGroupProps) {
  const { "report-group": groupFocus } = useExpansion();
  const expansionDispatch = useExpansionDispatch();
  const selectionDispatch = useSelectionDispatch();

  const groupKey = `${section}-${id}`;
  const expanded = !groupFocus.expansions[groupKey];

  const [hovered, setHovered] = useState(false);

  return (
    <div className="reports-group">
      <div
        className="reports-group-header"
        data-testid={`reports-group-${id}-header`}
        onClick={() => {
          if (id !== "other") {
            expansionDispatch({
              type: "toggle",
              group: "source-file",
              id,
              status: ExpansionStatus.Expanded,
            });
            selectionDispatch({
              scroll: "file",
              file: id,
            });
          }
        }}
      >
        <div
          className="reports-group-toggle"
          onClick={(e) => {
            e.stopPropagation();
            selectionDispatch({ scroll: "none" });
            expansionDispatch({
              type: "toggle",
              group: "report-group",
              id: groupKey,
              status: expanded
                ? ExpansionStatus.Collapsed
                : ExpansionStatus.Expanded,
            });
          }}
          onMouseEnter={() => setHovered(true)}
          onMouseLeave={() => setHovered(false)}
        >
          <FontAwesomeIcon
            icon={
              expanded
                ? hovered
                  ? solidMinus
                  : regularMinus
                : hovered
                  ? solidPlus
                  : regularPlus
            }
          />
        </div>
        {icon && (
          <div className="reports-group-filetype-icon">
            <FontAwesomeIcon icon={icon} />
          </div>
        )}
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
