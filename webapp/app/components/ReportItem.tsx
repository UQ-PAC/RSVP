import cx from "classnames";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faCircleExclamation,
  faCircleInfo,
  faCircleXmark,
} from "@fortawesome/free-solid-svg-icons";
import { Report, useReports, useReportsDispatch } from "../ReportsContext";

interface ReportItemParams {
  report: Report;
}

export function ReportItem({ report }: ReportItemParams) {
  const context = useReports();
  const dispatch = useReportsDispatch();

  const className = cx(
    "report-item",
    `report-item-${report.severity}`,
    context.selected === report.id &&
      context.hovered !== report.id &&
      "selected",
    context.hovered === report.id && "hovered",
  );

  const icon =
    report.severity === "err"
      ? faCircleXmark
      : report.severity === "warn"
        ? faCircleExclamation
        : faCircleInfo;

  return (
    <div
      className={className}
      onClick={() => dispatch({ type: "click", id: report.id })}
      onMouseEnter={() => dispatch({ type: "mouseEnter", id: report.id })}
      onMouseLeave={() => dispatch({ type: "mouseLeave", id: report.id })}
    >
      <FontAwesomeIcon className="report-item-icon" icon={icon} />
      <span
        className={`report-item-message report-item-message-${report.severity}`}
      >
        {report.message}
      </span>
    </div>
  );
}
