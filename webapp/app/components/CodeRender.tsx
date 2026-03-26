"use client";

import cx from "classnames";

import { JSX, useEffect, useRef } from "react";
import { Roboto_Mono } from "next/font/google";

import {
  Report,
  ReportSeverity,
  useSelection,
  useSelectionDispatch,
} from "../SelectionContext";
import hljs from "highlight.js";
import { renderToString } from "react-dom/server";

interface CodeRenderParams {
  content: string;
  reports: Report[];
  openReportsDrawer: () => void;
}

const robotoMono = Roboto_Mono({
  subsets: ["latin"],
});

// TODO:
//     line-based highlight (starts or ends mid-line)
//     block-based highlight (surrounded by /n)
//     scroll to primary or top unless explicitely clicked on line
// FIXME: multiple reports per line....?
export function CodeRender({
  content,
  reports,
  openReportsDrawer,
}: CodeRenderParams) {
  const { selected, hovered, scroll } = useSelection();
  const dispatch = useSelectionDispatch();

  // Scroll selected policy into view
  const focus = useRef<HTMLDivElement>(null);
  useEffect(() => {
    if (focus.current && scroll === "source") {
      focus.current.scrollIntoView({
        block: "center",
        inline: "center",
        behavior: "smooth",
      });
    }
  }, [scroll, focus, selected]);

  const reportsByLine: { [line: number]: Report[] } = {};

  reports.forEach((report) => {
    const loc = report.primarySourceLocation;
    const lines = content.slice(loc.offset, loc.offset + loc.len).split("\n");
    const nLines = lines.length;

    for (let line = loc.line; line < loc.line + nLines; line++) {
      if (!reportsByLine[line]) {
        reportsByLine[line] = [];
      }

      reportsByLine[line].push(report);
    }
  });

  const code: JSX.Element[] = [];
  hljs
    .highlight(content, {
      language: "cedar",
    })
    .value.split("\n")
    .forEach((line, i) => {
      const n = i + 1;
      const report = reportsByLine[n];

      const relevant = report?.reduce((result, current) => {
        switch (result.severity) {
          case "err":
            return result;
          case "warn":
            return current.severity === "err" ? current : result;
          case "info":
            return current;
        }
      });

      const className = cx(
        "source-file-line-content",
        report?.length && "source-report",
        relevant?.severity && `source-report-${relevant.severity}`,
        relevant && hovered === relevant.id && "hovered",
        relevant &&
          hovered !== relevant.id &&
          selected === relevant.id &&
          "selected",
      );

      code.push(
        <span key={`line-${n}`} className="source-file-line-number"></span>,
        <span
          key={n}
          className={className}
          data-report={relevant?.id}
          ref={
            relevant &&
            selected === relevant?.id &&
            relevant?.primarySourceLocation.line === n
              ? focus
              : undefined
          }
          dangerouslySetInnerHTML={{ __html: line }}
          onClick={
            relevant
              ? () => {
                  openReportsDrawer();
                  dispatch({
                    type: "click",
                    id: relevant?.id,
                    source: "source",
                  });
                }
              : undefined
          }
          onMouseEnter={
            relevant
              ? () =>
                  dispatch({
                    type: "mouseEnter",
                    id: relevant?.id,
                    source: "source",
                  })
              : undefined
          }
          onMouseLeave={
            relevant
              ? () =>
                  dispatch({
                    type: "mouseLeave",
                    id: relevant?.id,
                    source: "source",
                  })
              : undefined
          }
        ></span>,
      );
    });

  return (
    <div className="source-file-render">
      <pre className={`code ${robotoMono.className}`}>{code}</pre>
    </div>
  );
}
