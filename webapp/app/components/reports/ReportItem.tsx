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
  ExpansionState,
  useFocusDispatch,
} from "../../lib/context/FocusContext";
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

  const locDesc =
    report.sourceLocations.length == 0
      ? undefined
      : report.sourceLocations.length > 1
        ? "Multiple locations"
        : getSourceStr(report.sourceLocations[0].location);

  const clickLoc = (e, report: Report, loc?: SourceLoc) => {
    e.stopPropagation();
    const target = loc ?? report.sourceLocations[0].location;
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
      scroll: "source",
      loc: `${report.id}:${getSourceIdentifier(target)}`,
    });
    console.log(JSON.stringify(loc));
  };

  // TODO: refactor
  const enterLoc = (e, report: Report, loc?: SourceLoc) => {
    e.stopPropagation();
    const target = loc ?? report.sourceLocations[0].location;
    selectionDispatch({
      scroll: "none",
      hovered: "",
      loc: `${report.id}:${getSourceIdentifier(target)}`,
    });
  };

  return (
    <div
      id={`report-${report.id}`}
      ref={element}
      className={className}
      onClick={() => {
        if (!isSelected) {
          report.sourceLocations[0]?.location.source?.resolved
            .then((uploaded) => uploaded.serverId)
            .then((id) =>
              focusDispatch({
                type: "focus",
                target: "source-file",
                focus: { key: id, value: ExpansionState.Expanded },
              }),
            );
          selectionDispatch({
            selected: report.id,
            loc: `${report.id}:${getSourceIdentifier(report.sourceLocations[0].location)}`,
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
                className={className}
                onClick={(e) => clickLoc(e, report, location)}
                onMouseOver={(e) => enterLoc(e, report, location)}
                onMouseOut={() =>
                  selectionDispatch({
                    scroll: "none",
                    hovered: "",
                  })
                }
              >
                <span className="location">
                  {(location.file !== report.sourceLocations[0].location.file
                    ? location.source?.filename + ": "
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
