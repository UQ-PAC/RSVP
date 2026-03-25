"use client";

import cx from "classnames";

import { JSX, useEffect, useRef } from "react";
import { Roboto_Mono } from "next/font/google";

import {
  Report,
  useSelection,
  useSelectionDispatch,
} from "../SelectionContext";

interface CodeRenderParams {
  content: string;
  reports: Report[];
  openReportsDrawer: () => void;
}

const robotoMono = Roboto_Mono({
  subsets: ["latin"],
});

function getLineNumberInformation(
  source: string,
  offset: number,
): { line: number; col: number } | undefined {
  let line = 1;
  let col = 1;

  for (let i = 0; i < source.length; i++) {
    if (i === offset) {
      return { line, col };
    } else if (source.charAt(i) === "\n") {
      line++;
      col = 1;
    } else {
      col++;
    }
  }
}

export function CodeRender({
  content,
  reports,
  openReportsDrawer,
}: CodeRenderParams) {
  const { selected, hovered, scroll } = useSelection();
  const dispatch = useSelectionDispatch();

  const element = useRef<HTMLDivElement>(null);

  // Get line:col for offset & display
  // render <span> for each line
  // line-based highlight (starts or ends mid-line)
  // block-based highlight (surrounded by /n)
  // scroll to primary or top unless explicitely clicked on line

  useEffect(() => {
    if (scroll === "source") {
      element.current?.scrollIntoView({
        block: "center",
        inline: "center",
        behavior: "smooth",
      });
    }
  }, [scroll, element, selected]);

  const begin: string = content.slice(
    0,
    reports.at(0)?.primarySourceLocation.offset,
  );

  //   const lines: {
  //     content: string;
  //   }[] = [];

  //   let line = 1;
  //   let col = 1;

  //   for (let i = 0; i < content.length; i++) {
  //     if (content.charAt(i) === "\n") {
  //       lines.push(
  //         <span key={line} className={"code-render-line"} data-line={line}>
  //           {content.slice(i - col + 1, i + 1)}
  //         </span>,
  //       );
  //       line++;
  //       col = 1;
  //     } else {
  //       col++;
  //     }
  //   }

  // FIXME: multiple reports per line....?
  const blockReports: { [line: number]: Report[] } = {};
  const inlineReports: { [line: number]: Report[] } = {};

  reports.forEach((report) => {
    const primary = report.primarySourceLocation;

    if (primary) {
      const primaryContent = content.slice(
        primary.offset,
        primary.offset + primary.len,
      );
      const primaryLines = primaryContent.split("\n").length;

      const isBlock =
        primary.col === 0 &&
        content.charAt(primary.offset + primary.len) === "\n";

      for (
        let line = primary.line;
        line < primary.line + primaryLines;
        line++
      ) {
        if (isBlock) {
          if (!blockReports[line]) {
            blockReports[line] = [];
          }

          blockReports[line].push(report);
        } else {
          if (!inlineReports[line]) {
            inlineReports[line] = [];
          }

          inlineReports[line].push(report);
        }
      }
    }
  });

  return (
    <div className="source-file-render">
      <pre className={`code ${robotoMono.className}`}>
        {content.split("\n").map((line, i) => (
          <>
            <span key={`#${i}`} className="source-file-line-number"></span>
            <span key={i} className="source-file-line-content">
              {line + "\n"}
            </span>
          </>
        ))}
        {/* {lines} */}
        {/* {begin}
      {reports.map((report: Report, index) => (
        <span key={report.id}>
          <span
            id={`source-report-${report.id}`}
            ref={selected === report.id ? element : null}
            className={cx(
              "source-report",
              `source-report-${report.severity}`,
              selected === report.id && hovered !== report.id && "selected",
              hovered === report.id && "hovered",
            )}
            data-message={report.message}
            onClick={() => {
              openReportsDrawer();
              dispatch({ type: "click", id: report.id, source: "source" });
            }}
            onMouseEnter={() =>
              dispatch({
                type: "mouseEnter",
                id: report.id,
                source: "source",
              })
            }
            onMouseLeave={() =>
              dispatch({
                type: "mouseLeave",
                id: report.id,
                source: "source",
              })
            }
          >
            {content.slice(
              report.primarySourceLocation.offset,
              report.primarySourceLocation.offset +
                report.primarySourceLocation.len,
            )}
          </span>
          {content.slice(
            report.primarySourceLocation.offset +
              report.primarySourceLocation.len,
            index == reports.length - 1
              ? content.length
              : reports.at(index + 1)?.primarySourceLocation.offset,
          )}
        </span>
      ))} */}
      </pre>
    </div>
  );
}
