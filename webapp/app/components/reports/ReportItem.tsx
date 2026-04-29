import {
  faCaretDown,
  faCaretUp,
  faCircleExclamation,
  faCircleInfo,
  faCircleXmark,
} from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import cx from "classnames";
import { useEffect, useRef } from "react";
import { Report, SourceLoc } from "../../types";
import { getIdentifier, toSourceStr } from "../../util";
import { ExpansionState, useFocusDispatch } from "../providers/FocusContext";
import {
  useSelection,
  useSelectionDispatch,
} from "../providers/SelectionContext";

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

  const locDesc = report.sourceLocations.length
    ? "Multiple locations"
    : toSourceStr(report.primarySourceLocation);

  const clickLoc = (e, report: Report, loc?: SourceLoc) => {
    e.stopPropagation();
    const target = loc ?? report.primarySourceLocation;
    target.source?.resolved
      .then((uploaded) => uploaded.serverId)
      .then((id) =>
        focusDispatch({
          type: "focus",
          target: "source-file",
          focus: { key: id, value: ExpansionState.Expanded },
        }),
      );
    selectionDispatch({
      type: "click",
      scroll: "source",
      loc: `${report.id}:${getIdentifier(target)}`,
    });
  };

  const enterLoc = (e, report: Report, loc?: SourceLoc) => {
    const target = loc ?? report.primarySourceLocation;
    selectionDispatch({
      type: "mouseEnter",
      scroll: "none",
      loc: `${report.id}:${getIdentifier(target)}`,
    });
  };

  const leaveLoc = (e, report: Report, loc?: SourceLoc) => {
    const target = loc ?? report.primarySourceLocation;
    selectionDispatch({
      type: "mouseLeave",
      scroll: "none",
      loc: `${report.id}:${getIdentifier(target)}`,
    });
  };

  return (
    <div
      id={`report-${report.id}`}
      ref={element}
      className={className}
      onClick={() => {
        if (!isSelected) {
          report.primarySourceLocation.source?.resolved
            .then((uploaded) => uploaded.serverId)
            .then((id) =>
              focusDispatch({
                type: "focus",
                target: "source-file",
                focus: { key: id, value: ExpansionState.Expanded },
              }),
            );
        }
        selectionDispatch({ type: "click", id: report.id, scroll: "source" });
      }}
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
      <div className="report-item-header">
        <FontAwesomeIcon
          className="report-item-icon report-item-icon-severity"
          icon={icon}
        />
        <span
          className={`report-item-message report-item-message-${report.severity}`}
        >
          <span className="report-item-line-info">{locDesc}:</span>{" "}
          {report.message}
        </span>

        {(!!report.messageDetail?.length ||
          !!report.sourceLocations.length) && (
          <FontAwesomeIcon
            className="report-item-icon report-item-icon-expand"
            icon={isSelected ? faCaretUp : faCaretDown}
          />
        )}
      </div>
      {isSelected && !!report.sourceLocations.length && (
        <div className="report-item-source-location-list">
          <span
            className="report-item-source-location"
            onClick={(e) => clickLoc(e, report)}
            onMouseEnter={(e) => enterLoc(e, report)}
            onMouseLeave={(e) => leaveLoc(e, report)}
          >
            {toSourceStr(report.primarySourceLocation)}
          </span>
          {report.sourceLocations.map((loc) => (
            <span
              key={getIdentifier(loc)}
              className="report-item-source-location"
              onClick={(e) => clickLoc(e, report, loc)}
              onMouseEnter={(e) => enterLoc(e, report, loc)}
              onMouseLeave={(e) => leaveLoc(e, report, loc)}
            >
              {toSourceStr(loc)}
            </span>
          ))}
        </div>
      )}
      {isSelected && !!report.messageDetail?.length && (
        <div className="report-item-message-detail">{report.messageDetail}</div>
      )}
    </div>
  );
}
