"use client";

import cx from "classnames";
import hljs from "highlight.js";
import { JSX, Ref, useMemo } from "react";
import { FileType, Report, ReportSeverity, SourceLoc } from "../../types";
import { getSourceIdentifier } from "../../util";

export interface ReportLine {
  report: Report;
  loc: SourceLoc;
  start?: number;
  end?: number;
}

interface CodeLineProps {
  line: string;
  n: number;
  syntax: FileType;
  reports: ReportLine[];
  focus?: Ref<HTMLSpanElement>;
  selected?: string;
  hovered?: string;
  selectedLoc?: string;
  onclick: (id: string, severity: ReportSeverity, loc: SourceLoc) => void;
  onenter: (id: string) => void;
  onleave: (id: string) => void;
}

function sortByPrecedence(a: ReportLine, b: ReportLine): number {
  if (a.loc.offset === b.loc.offset) {
    return a.loc.len - b.loc.len;
  }

  return b.loc.offset - a.loc.offset;
}

export function CodeLine({
  line,
  n,
  focus,
  syntax,
  reports,
  selected,
  hovered,
  selectedLoc,
  onclick,
  onenter,
  onleave,
}: CodeLineProps) {
  const { reportsByPrecedence, mostSevere, mostRelevant } = useMemo(() => {
    const reportsByPrecedence = reports?.sort(sortByPrecedence);

    const mostSevere = reports?.reduce((result, current) => {
      switch (result.report.severity) {
        case "err":
          return result;
        case "warn":
          return current.report.severity === "err" ? current : result;
        case "info":
          return current;
      }
    });

    const mostRelevant = reportsByPrecedence?.at(0);

    return { reportsByPrecedence, mostSevere, mostRelevant };
  }, [reports]);

  const highlight = (text: string) =>
    syntax !== "text"
      ? hljs.highlight(text, {
          language: syntax,
        }).value
      : text;

  const click = (e, id: string, severity: ReportSeverity, loc: SourceLoc) => {
    e.stopPropagation();
    onclick(id, severity, loc);
  };

  const enter = (e, id: string) => {
    e.stopPropagation();
    onenter(id);
  };

  const leave = (e, id: string) => {
    // e.preventDefault();
    onleave(id);
  };

  if (reports?.length) {
    let selectedReport: Report | undefined = undefined;
    let hoveredReport: Report | undefined = undefined;

    // Storage for gradual nesting of report spans
    let child: JSX.Element | undefined = undefined;

    // Possible reference to topmost line of selected report (for scroll focus)
    let ref: Ref<HTMLSpanElement> | undefined = undefined;

    let i = 0;
    for (const report of reportsByPrecedence) {
      const severity = report.report.severity;
      const loc = report.loc;

      const start = report?.start ? report.start : 0;
      const end = report?.end;

      const id = report.report.id;

      const partial = !!report?.start || !!report?.end;

      const innermost = i === 0;
      const outermost = i === reportsByPrecedence.length - 1;

      const targetLoc = selectedLoc
        ? selectedLoc === `${id}:${getSourceIdentifier(loc)}`
        : true;

      const outerHover = reportsByPrecedence.some(
        (report, j) => j > i && report.report.id === hovered,
      );

      if (hovered === id && targetLoc) {
        hoveredReport = report.report;
      } else if (
        // Only render selection if the selected report is not nested inside
        // a hovered report
        selected === id &&
        !outerHover
      ) {
        selectedReport = report.report;
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
        !!hoveredReport && "hovered",
        !hoveredReport && !!selectedReport && "selected",
      );

      if (innermost) {
        // Inner-most (most nested) report
        child = (
          <span
            className={className}
            onClick={partial ? (e) => click(e, id, severity, loc) : undefined}
            onMouseEnter={partial ? (e) => enter(e, id) : undefined}
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
            onClick={partial ? (e) => click(e, id, severity, loc) : undefined}
            onMouseEnter={partial ? (e) => enter(e, id) : undefined}
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
      !hoveredReport &&
        !!selectedReport &&
        `selected-${selectedReport.severity}`,
    );

    return (
      <>
        <span className={numberClass} />
        <span
          className={contentClass}
          ref={ref}
          onClick={
            mostRelevant
              ? (e) =>
                  click(
                    e,
                    mostRelevant?.report.id,
                    mostRelevant?.report.severity,
                    mostRelevant?.loc,
                  )
              : undefined
          }
          onMouseEnter={
            mostRelevant ? (e) => enter(e, mostRelevant?.report.id) : undefined
          }
          onMouseLeave={
            mostRelevant ? (e) => leave(e, mostRelevant?.report.id) : undefined
          }
        >
          {child}
        </span>
      </>
    );
  } else {
    // No reports, just highlight the line
    return (
      <>
        <span className="source-file-line-number" />
        <span
          className="source-file-line-content"
          dangerouslySetInnerHTML={{ __html: highlight(line) }}
        />
      </>
    );
  }
}
