import cx from "classnames";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faCaretDown,
  faCaretUp,
  faCircleExclamation,
  faCircleInfo,
  faCircleXmark,
} from "@fortawesome/free-solid-svg-icons";
import {
  useSelection,
  useSelectionDispatch,
} from "../providers/SelectionContext";
import { Report } from "../../types";
import { useEffect, useRef } from "react";
import { ExpansionState, useFocusDispatch } from "../providers/FocusContext";

interface ReportItemParams {
  report: Report;
}

export function ReportItem({ report }: ReportItemParams) {
  const { selected, hovered, scroll } = useSelection();
  const selectionDispatch = useSelectionDispatch();
  const focusDispatch = useFocusDispatch();

  const element = useRef<HTMLDivElement>(null);

  const isSelected = selected === report.id;
  const isHovered = hovered === report.id;

  useEffect(() => {
    if (scroll === "report" && report.id === selected) {
      element.current?.scrollIntoView({
        block: "center",
        inline: "center",
        behavior: "smooth",
      });
    }
  }, [scroll, element, report.id, selected]);

  const className = cx(
    "report-item",
    `report-item-${report.severity}`,
    isSelected && !isHovered && "selected",
    isHovered && "hovered",
  );

  const icon =
    report.severity === "err"
      ? faCircleXmark
      : report.severity === "warn"
        ? faCircleExclamation
        : faCircleInfo;

  // line info || entire file
  // need version info for policy file reports (only if more than one version exists)

  return (
    <div
      id={`report-${report.id}`}
      ref={element}
      className={className}
      onMouseEnter={() =>
        selectionDispatch({
          type: "mouseEnter",
          id: report.id,
          scroll: "none",
        })
      }
      onMouseLeave={() =>
        selectionDispatch({
          type: "mouseLeave",
          id: report.id,
          scroll: "none",
        })
      }
    >
      <div
        className="report-item-header"
        onClick={() => {
          if (!isSelected) {
            const filename = report.primarySourceLocation.source?.file.name;
            if (filename) {
              focusDispatch({
                type: "source-file",
                key: filename,
                value: ExpansionState.Expanded,
              });
            }
          }
          selectionDispatch({ type: "click", id: report.id, scroll: "source" });
        }}
      >
        <FontAwesomeIcon
          className="report-item-icon report-item-icon-severity"
          icon={icon}
        />
        <span
          className={`report-item-message report-item-message-${report.severity}`}
        >
          {report.message}
          <span className="report-item-line-info">
            {`  (${report.primarySourceLocation.source?.file.name}:${report.primarySourceLocation.line}:${report.primarySourceLocation.col})`}
          </span>
        </span>

        {!!report.messageDetail?.length && (
          <FontAwesomeIcon
            className="report-item-icon report-item-icon-expand"
            icon={isSelected ? faCaretUp : faCaretDown}
          />
        )}
      </div>
      {isSelected && !!report.messageDetail?.length && (
        <div className="report-item-message-detail">{report.messageDetail}</div>
      )}
    </div>
  );
}
