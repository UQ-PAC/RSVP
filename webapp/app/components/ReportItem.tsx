import cx from "classnames";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { Report } from "./SourceFile";
import {
  faCircleExclamation,
  faCircleInfo,
  faCircleXmark,
} from "@fortawesome/free-solid-svg-icons";

interface ReportItemParams {
  report: Report;
  selected: boolean;
  active: boolean;
  onclick: (id: string) => void;
  onactivate: (id: string) => void;
  ondeactivate: (id: string) => void;
}

export function ReportItem({
  report,
  selected,
  active,
  onclick,
  onactivate,
  ondeactivate,
}: ReportItemParams) {
  const icon =
    report.severity === "err"
      ? faCircleXmark
      : report.severity === "warn"
        ? faCircleExclamation
        : faCircleInfo;

  return (
    <div
      className={cx(
        "report-item",
        `report-item-${report.severity}`,
        selected && !active && "selected",
        active && "active",
      )}
      onClick={() => onclick(report.id)}
      onMouseEnter={() => onactivate(report.id)}
      onMouseLeave={() => ondeactivate(report.id)}
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
