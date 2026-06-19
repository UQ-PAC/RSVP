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
import {
  ExpansionStatus,
  useExpansionDispatch,
} from "../../lib/context/ExpansionContext";
import {
  useSelection,
  useSelectionDispatch,
} from "../../lib/context/SelectionContext";
import { Report, SourceLoc } from "../../lib/types";
import { getSourceIdentifier, getSourceStr } from "../../lib/util";

interface ReportItemParams {
  report: Report;
}

export function ReportItem({ report }: ReportItemParams) {
  const { selected, hovered, scroll, loc } = useSelection();
  const selectionDispatch = useSelectionDispatch();
  const expansionDispatch = useExpansionDispatch();

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

  const locDesc =
    report.sourceLocations.length == 0
      ? undefined
      : report.sourceLocations.length > 1
        ? "Multiple locations"
        : getSourceStr(report.sourceLocations[0].location);

  const clickLoc = (e, report: Report, loc: SourceLoc) => {
    e.stopPropagation();
    expansionDispatch({
      type: "toggle",
      group: "source-file",
      id: loc.file,
      status: ExpansionStatus.Expanded,
    });
    selectionDispatch({
      scroll: "source",
      loc: `${report.id}:${getSourceIdentifier(loc)}`,
    });
  };

  // TODO: refactor
  const enterLoc = (e, report: Report, loc: SourceLoc) => {
    e.stopPropagation();
    selectionDispatch({
      scroll: "none",
      hovered: "",
      loc: `${report.id}:${getSourceIdentifier(loc)}`,
    });
  };

  return (
    <div
      id={`report-${report.id}`}
      ref={element}
      className={className}
      data-testid="report-item"
      onClick={() => {
        if (!isSelected) {
          const target = report.sourceLocations[0];
          if (target?.location) {
            expansionDispatch({
              type: "toggle",
              group: "source-file",
              id: target.location.file,
              status: ExpansionStatus.Expanded,
            });
          }
          selectionDispatch({
            selected: report.id,
            loc: target
              ? `${report.id}:${getSourceIdentifier(report.sourceLocations[0].location)}`
              : undefined,
            scroll: "source",
          });
        } else {
          selectionDispatch({
            selected: "",
            scroll: "none",
            loc: undefined,
          });
        }
      }}
      onMouseOver={() =>
        selectionDispatch({
          hovered: report.id,
          scroll: "none",
          loc: undefined,
        })
      }
      onMouseOut={() =>
        selectionDispatch({
          hovered: "",
          scroll: "none",
          loc: undefined,
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
          {locDesc && (
            <span className="report-item-line-info">{locDesc}: </span>
          )}
          {report.message}
        </span>

        {(!!report.messageDetail?.length ||
          report.sourceLocations.length > 1) && (
          <FontAwesomeIcon
            className="report-item-icon report-item-icon-expand"
            icon={isSelected ? faCaretUp : faCaretDown}
          />
        )}
      </div>
      {isSelected && !!report.messageDetail?.length && (
        <div className="report-item-message-detail">{report.messageDetail}</div>
      )}
      {isSelected && report.sourceLocations.length > 1 && (
        <div className="report-item-source-location-list">
          {report.sourceLocations.map(({ message, location }) => {
            const id = getSourceIdentifier(location);
            const className = cx(
              "report-item-source-location",
              loc === `${report.id}:${id}` && "hovered",
            );

            return (
              <span
                key={id}
                data-testid={`report-item-source-location-${id}`}
                className={className}
                onClick={(e) => clickLoc(e, report, location)}
                onMouseOver={(e) => enterLoc(e, report, location)}
                onMouseOut={(e) => {
                  e.stopPropagation();
                  selectionDispatch({
                    scroll: "none",
                    hovered: "",
                    loc: undefined,
                  });
                }}
              >
                <span className="location">
                  {(location.file !== report.sourceLocations[0].location.file
                    ? (location.source?.filename ?? "Unknown file") + ": "
                    : "") + getSourceStr(location)}
                </span>

                {!!message && <span className="message">: {message}</span>}
              </span>
            );
          })}
        </div>
      )}
    </div>
  );
}
