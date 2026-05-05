"use client";

import cx from "classnames";
import { JSX, Ref, useEffect, useMemo, useRef } from "react";
import {
  ExpansionState,
  useFocus,
  useFocusDispatch,
} from "../../lib/context/FocusContext";
import {
  useSelection,
  useSelectionDispatch,
} from "../../lib/context/SelectionContext";
import { ReportLine } from "../../lib/sources/types";
import {
  getHighlightFunction,
  sortReportLinesByPrecedence,
} from "../../lib/sources/util";
import { FileType, Report, SourceLoc } from "../../lib/types";
import { getSourceIdentifier } from "../../lib/util";

interface HighlightedCodeLineProps {
  line: string;
  n: number;
  syntax: FileType;
  reports: ReportLine[];
}

export function HighlightedCodeLine({
  line,
  n,
  syntax,
  reports,
}: HighlightedCodeLineProps) {
  const { selected, hovered, scroll, loc: selectedLoc } = useSelection();
  const selectionDispatch = useSelectionDispatch();

  const { drawer: drawerFocus } = useFocus();
  const focusDispatch = useFocusDispatch();

  // Scroll selected policy into view
  const focus = useRef<HTMLSpanElement>(null);
  useEffect(() => {
    if (focus.current && scroll === "source") {
      focus.current.scrollIntoView({
        block: "center",
        inline: "center",
        behavior: "smooth",
      });
    }
  }, [scroll, focus, selected, selectedLoc]);

  const { reportsByPrecedence, mostSevere, mostRelevant } = useMemo(
    () => sortReportLinesByPrecedence(reports),
    [reports],
  );

  const highlight = getHighlightFunction(syntax);

  const click = (e, report: Report, loc: SourceLoc) => {
    e.stopPropagation();
    const id = report.id;
    const severity = report.severity;
    if (selected !== id) {
      if (drawerFocus.expansions.right !== ExpansionState.Expanded) {
        focusDispatch({
          type: "focus",
          target: "drawer",
          focus: { key: "left", value: ExpansionState.Collapsed },
        });
        focusDispatch({
          type: "focus",
          target: "drawer",
          focus: {
            key: "right",
            value: ExpansionState.Expanded,
          },
        });
      }

      focusDispatch({
        type: "focus",
        target: "report-group",
        focus: {
          key: `${severity}-${loc.source?.filename}`,
          value: ExpansionState.Expanded,
        },
      });
    }

    const selecting = selected !== id;
    const targetLoc =
      report.sourceLocations.length > 1
        ? `${report.id}:${getSourceIdentifier(loc ?? report.sourceLocations[0].location)}`
        : undefined;

    selectionDispatch({
      scroll: selecting ? "report" : "none",
      selected: selecting ? id : undefined,
      hovered: id,
      loc: targetLoc,
    });
  };

  const enter = (e, report: Report, loc?: SourceLoc) => {
    e.stopPropagation();
    if (selected === report.id && report.sourceLocations.length > 1) {
      const target = loc ?? report.sourceLocations[0].location;
      selectionDispatch({
        hovered: "",
        scroll: "none",
        loc: `${report.id}:${getSourceIdentifier(target)}`,
      });
    } else {
      selectionDispatch({
        hovered: report.id,
        scroll: "none",
      });
    }
  };

  let selectedReport: Report | undefined = undefined;
  let hoveredReport: Report | undefined = undefined;

  // Storage for gradual nesting of report spans
  let child: JSX.Element | undefined = undefined;

  // Possible reference to topmost line of selected report (for scroll focus)
  let ref: Ref<HTMLSpanElement> | undefined = undefined;

  let i = 0;
  for (const report of reportsByPrecedence) {
    const loc = report.loc;

    const start = report?.start ? report.start : 0;
    const end = report?.end;

    const id = report.report.id;

    const partial = !!report?.start || !!report?.end;

    const innermost = i === 0;
    const outermost = i === reportsByPrecedence.length - 1;

    let thisReportHovered = false;
    let thisReportSelected = false;

    const targetReport = !selectedLoc && hovered === id;

    const targetLoc = selectedLoc
      ? selectedLoc === `${id}:${getSourceIdentifier(loc)}`
      : false;

    const outerHover = reportsByPrecedence.some(
      (report, j) => j > i && targetReport && report.report.id === hovered,
    );

    if (targetReport || targetLoc) {
      hoveredReport = report.report;
      thisReportHovered = true;
    } else if (
      // Only render selection if the selected report is not nested inside
      // a hovered report
      selected === id &&
      !outerHover
    ) {
      selectedReport = report.report;
      thisReportSelected = true;
    }

    // Scroll to this line
    if (selected === id && loc.startLoc?.line === n && targetLoc) {
      ref = focus;
    }

    const className = cx(
      "source-report-text-highlight",
      partial &&
        (selectedReport || hoveredReport) &&
        `source-report-text-highlight-${report.report.severity}`,
      thisReportHovered && "hovered",
      !thisReportHovered && thisReportSelected && "selected",
    );

    if (innermost) {
      // Inner-most (most nested) report
      child = (
        <span
          className={className}
          onClick={partial ? (e) => click(e, report.report, loc) : undefined}
          onMouseOver={
            partial ? (e) => enter(e, report.report, loc) : undefined
          }
          dangerouslySetInnerHTML={{
            __html: highlight(line.slice(start, end)),
          }}
        />
      );
    } else {
      // Report containing nested reports.
      const prev = reportsByPrecedence[i - 1];
      const nestedStart = prev.start ? prev.start : 0;
      const nestedEnd = prev?.end;

      child = (
        <span
          className={className}
          onClick={partial ? (e) => click(e, report.report, loc) : undefined}
          onMouseOver={
            partial ? (e) => enter(e, report.report, loc) : undefined
          }
        >
          {start !== nestedStart && (
            <span
              dangerouslySetInnerHTML={{
                __html: highlight(line.slice(start, nestedStart)),
              }}
            />
          )}
          {child}
          {end !== nestedEnd && (
            <span
              dangerouslySetInnerHTML={{
                __html: highlight(line.slice(nestedEnd, end)),
              }}
            />
          )}
        </span>
      );
    }

    if (outermost) {
      // Outer-most report, add any non-highlighted text to the start and/or end of the line
      const before = start ? line.slice(0, start) : undefined;
      const after = end ? line.slice(end) : undefined;

      if (before || after) {
        child = (
          <span>
            {before && (
              <span
                dangerouslySetInnerHTML={{
                  __html: highlight(before),
                }}
              />
            )}
            {child}
            {after && (
              <span
                dangerouslySetInnerHTML={{
                  __html: highlight(after),
                }}
              />
            )}
          </span>
        );
      }
    }

    i++;
  }

  const severity = (hoveredReport ?? selectedReport ?? mostSevere.report)
    .severity;

  const numberClass = cx(
    "source-file-line-number",
    reports?.length && "source-report",
    `source-report-${severity}`,
    (!!hoveredReport || !!selectedReport) &&
      `selected-${(hoveredReport ?? selectedReport)?.severity}`,
  );

  const contentClass = cx(
    "source-file-line-content",
    "source-report",
    `source-report-${severity}`,
    !!hoveredReport && `hovered-${hoveredReport.severity}`,
    !hoveredReport && !!selectedReport && `selected-${selectedReport.severity}`,
  );

  return (
    <>
      <span className={numberClass} />
      <span
        className={contentClass}
        ref={ref}
        onClick={
          mostRelevant
            ? (e) => click(e, mostRelevant?.report, mostRelevant?.loc)
            : undefined
        }
        onMouseOver={
          mostRelevant
            ? (e) => enter(e, mostRelevant?.report, mostRelevant?.loc)
            : undefined
        }
        onMouseOut={
          mostRelevant
            ? () =>
                selectionDispatch({
                  scroll: "none",
                  loc: "",
                  hovered: "",
                })
            : undefined
        }
      >
        {child}
      </span>
    </>
  );
}
