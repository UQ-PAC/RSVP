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
  Report,
  useSelection,
  useSelectionDispatch,
} from "../SelectionContext";
import { useEffect, useRef } from "react";

interface ReportItemParams {
  report: Report;
}

export function ReportItem({ report }: ReportItemParams) {
  const { selected, hovered, scroll } = useSelection();
  const dispatch = useSelectionDispatch();

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
        dispatch({ type: "mouseEnter", id: report.id, source: "report" })
      }
      onMouseLeave={() =>
        dispatch({ type: "mouseLeave", id: report.id, source: "report" })
      }
    >
      <div
        className="report-item-header"
        onClick={() =>
          dispatch({ type: "click", id: report.id, source: "report" })
        }
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
            {`  (${report.primarySourceLocation.source?.filename}:${report.primarySourceLocation.line}:${report.primarySourceLocation.col})`}
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
