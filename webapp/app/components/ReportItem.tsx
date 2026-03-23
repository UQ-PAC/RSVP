import cx from "classnames";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faCircleExclamation,
  faCircleInfo,
  faCircleXmark,
} from "@fortawesome/free-solid-svg-icons";
import {
  Report,
  useSelection,
  useSelectionDispatch,
} from "../SelectionContext";

interface ReportItemParams {
  report: Report;
}

export function ReportItem({ report }: ReportItemParams) {
  const { selected, hovered } = useSelection();
  const dispatch = useSelectionDispatch();

  const className = cx(
    "report-item",
    `report-item-${report.severity}`,
    selected === report.id && hovered !== report.id && "selected",
    hovered === report.id && "hovered",
  );

  const icon =
    report.severity === "err"
      ? faCircleXmark
      : report.severity === "warn"
        ? faCircleExclamation
        : faCircleInfo;

  return (
    <div
      id={`report-${report.id}`}
      className={className}
      onClick={() =>
        dispatch({ type: "click", id: report.id, source: "report" })
      }
      onMouseEnter={() =>
        dispatch({ type: "mouseEnter", id: report.id, source: "report" })
      }
      onMouseLeave={() =>
        dispatch({ type: "mouseLeave", id: report.id, source: "report" })
      }
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
