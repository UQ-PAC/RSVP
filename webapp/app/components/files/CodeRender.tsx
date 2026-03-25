"use client";

import cx from "classnames";
import hljs from "highlight.js";

import { JSX, useEffect, useRef } from "react";
import { Roboto_Mono } from "next/font/google";

import { Report } from "../../types";
import {
  useSelection,
  useSelectionDispatch,
} from "../providers/SelectionContext";
import {
  ExpansionState,
  useFocus,
  useFocusDispatch,
} from "../providers/FocusContext";
import { renderToString } from "react-dom/server";

interface CodeRenderParams {
  content: string;
  reports: Report[];
}

const robotoMono = Roboto_Mono({
  subsets: ["latin"],
});

// TODO:
//     line-based highlight (starts or ends mid-line)
//     scroll to primary or top unless explicitely clicked on line
// FIXME: multiple reports per line....?
export function CodeRender({ content, reports }: CodeRenderParams) {
  const { selected, hovered, scroll } = useSelection();
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
  }, [scroll, focus, selected]);

  const reportsByLine: {
    [line: number]: { report: Report; start?: number; end?: number }[];
  } = {};

  reports.forEach((report) => {
    const loc = report.primarySourceLocation;
    const lines = content.slice(loc.offset, loc.offset + loc.len).split("\n");
    const nLines = lines.length;

    let offset = 0;

    for (let line = loc.line; line < loc.line + nLines; line++) {
      if (!reportsByLine[line]) {
        reportsByLine[line] = [];
      }

      let start: number | undefined = undefined;
      let end: number | undefined = undefined;

      if (line === loc.line && loc.col !== 1) {
        start = loc.col - 1;
      }

      if (
        line === loc.line + nLines &&
        content.charAt(loc.offset + loc.len) !== "\n"
      ) {
        end = loc.offset + loc.len - offset - 1;
      }

      reportsByLine[line].push({ report, start, end });
      offset += lines[line - loc.line].length + 1;
    }
  });

  const code: JSX.Element[] = [];
  content.split("\n").forEach((line, i) => {
    const n = i + 1;
    const report = reportsByLine[n];

    // FIXME: multiple reports per line
    const relevant = report?.reduce((result, current) => {
      switch (result.report.severity) {
        case "err":
          return result;
        case "warn":
          return current.report.severity === "err" ? current : result;
        case "info":
          return current;
      }
    });

    const id = relevant?.report.id;

    const isSelected = relevant && hovered !== id && selected === id;
    const isHovered = relevant && hovered === id;

    const className = cx(
      "source-file-line-content",
      report?.length && "source-report",
      relevant?.report.severity && `source-report-${relevant.report.severity}`,
      isHovered && "hovered",
      isSelected && "selected",
    );

    let highlighted: string | undefined = undefined;

    if (relevant?.start || relevant?.end) {
      const begin = relevant?.start
        ? hljs.highlight(line.slice(0, relevant.start), {
            language: "cedar",
          }).value
        : "";
      const mid =
        `<span class="source-report-text-highlight source-report-text-highlight-${relevant?.report.severity}"` +
        `data-report="${relevant?.report.id}">${
          hljs.highlight(line.slice(relevant?.start ?? 0, relevant?.end), {
            language: "cedar",
          }).value
        }</span>`;
      const end = relevant?.end
        ? hljs.highlight(line.slice(relevant.end), {
            language: "cedar",
          }).value
        : "";

      highlighted = begin + mid + end;
    } else {
      highlighted = hljs.highlight(line, {
        language: "cedar",
      }).value;
    }

    // console.log(line);
    code.push(
      <span key={`line-${n}`} className="source-file-line-number"></span>,
      <span
        key={n}
        className={className}
        data-report={id}
        ref={
          relevant &&
          selected === id &&
          relevant?.report.primarySourceLocation.line === n
            ? focus
            : undefined
        }
        dangerouslySetInnerHTML={{ __html: highlighted }}
        onClick={
          relevant
            ? () => {
                if (selected !== id) {
                  if (drawerFocus["right"] !== ExpansionState.Expanded) {
                    focusDispatch({
                      type: "drawer",
                      key: "left",
                      value: ExpansionState.Collapsed,
                    });
                    focusDispatch({
                      type: "drawer",
                      key: "right",
                      value: ExpansionState.Expanded,
                    });
                  }

                  focusDispatch({
                    type: "report-group",
                    key: `${relevant?.report.severity}-${relevant?.report.primarySourceLocation.source?.filename}`,
                    value: ExpansionState.Expanded,
                  });
                }

                selectionDispatch({
                  type: "click",
                  id: id,
                  scroll: selected !== id ? "report" : "none",
                });
              }
            : undefined
        }
        onMouseEnter={
          relevant
            ? () =>
                selectionDispatch({
                  type: "mouseEnter",
                  id: id,
                  scroll: "none",
                })
            : undefined
        }
        onMouseLeave={
          relevant
            ? () =>
                selectionDispatch({
                  type: "mouseLeave",
                  id: id,
                  scroll: "none",
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
