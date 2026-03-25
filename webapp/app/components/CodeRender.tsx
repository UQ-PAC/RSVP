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

export function CodeRender({
  content,
  reports,
  openReportsDrawer,
}: CodeRenderParams) {
  const { selected, hovered, scroll } = useSelection();
  const dispatch = useSelectionDispatch();

  const element = useRef<HTMLDivElement>(null);

  // TODO:
  //     line-based highlight (starts or ends mid-line)
  //     block-based highlight (surrounded by /n)
  //     scroll to primary or top unless explicitely clicked on line

  const code = useRef<HTMLPreElement>(null);

  // Scroll selected policy into view
  useEffect(() => {
    if (scroll === "source") {
      element.current?.scrollIntoView({
        block: "center",
        inline: "center",
        behavior: "smooth",
      });
    }
  }, [scroll, element, selected]);

  const blockReports: {
    [line: number]: { id: string; severity: ReportSeverity; anchor: boolean }[];
  } = {};
  const inlineReports: {
    [line: number]: {
      id: string;
      severity: ReportSeverity;
      anchor: boolean;
      start?: number;
      end?: number;
    }[];
  } = {};

  console.log("reports: " + reports.length);

  reports.forEach((report) => {
    const loc = report.primarySourceLocation;
    // console.log(JSON.stringify(report.primarySourceLocation));

    if (loc) {
      const lines = content.slice(loc.offset, loc.offset + loc.len).split("\n");
      const nLines = lines.length;

      const isBlock =
        loc.col === 1 && content.charAt(loc.offset + loc.len) === "\n";

      console.log("isBlock: " + isBlock);
      console.log("end: '" + content.charAt(loc.offset + loc.len) + "'");

      for (let line = loc.line; line < loc.line + nLines; line++) {
        if (isBlock) {
          if (!blockReports[line]) {
            blockReports[line] = [];
          }

          blockReports[line].push({
            id: report.id,
            severity: report.severity,
            anchor: line === loc.line,
          });
        } else {
          if (!inlineReports[line]) {
            inlineReports[line] = [];
          }

          inlineReports[line].push({
            id: report.id,
            severity: report.severity,
            anchor: line === loc.line,
            start: line == loc.line ? loc.col : 1,
            end: line == loc.line + nLines ? lines[nLines - 1].length : 1,
          });
        }
      }
    }
  });

  console.log(JSON.stringify(blockReports));

  useEffect(() => {
    console.log("useEffect");

    if (code.current) {
      console.log("setting innerHTML");
      //   hljs.highlightElement(code.current);
      code.current.innerHTML = hljs
        .highlight(content, {
          language: "cedar",
        })
        .value.split("\n")
        .map((line, i) => {
          const blockReport = blockReports[i + 1];

          const { severity, id, anchor } = blockReport?.reduce(
            (result, current) => {
              switch (result.severity) {
                case "err":
                  return result;
                case "warn":
                  return current.severity === "err" ? current : result;
                case "info":
                  return current;
              }
            },
          ) ?? { severity: undefined, id: undefined, anchor: false };

          const className = cx(
            "source-file-line-content",
            blockReport?.length && "source-file-report",
            severity && `source-file-report-${severity}`,
          );

          const lineNo = renderToString(
            <span className="source-file-line-number"></span>,
          );
          //   const lineContent = renderToString(
          //     <span className={className} data-report={id} data-anchor={anchor}>
          //       ${line}
          //     </span>,
          //   );
          const lineContent = `<span class="${className}"${id ? ` data-report="${id}"` : ""}${anchor ? ` data-anchor=${anchor}` : ""}>${line}</span>`;
          return lineNo + lineContent;
        })
        .join("");
    }
  }, [code, content, reports, blockReports]);

  //   const begin: string = content.slice(
  //     0,
  //     reports.at(0)?.primarySourceLocation.offset,
  //   );

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

  return (
    <div className="source-file-render">
      <pre ref={code} className={`code ${robotoMono.className}`}>
        {content}
        {/* {hljs.highlight(content, { language: "cedar" }).value} */}
        {/* {content.split("\n").map((line, i) => (
          <>
            <span key={`#${i}`} className="source-file-line-number"></span>
            <span key={i} className="source-file-line-content">
              {line + "\n"}
            </span>
          </>
        ))} */}
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
