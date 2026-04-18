"use client";

import cx from "classnames";
import hljs from "highlight.js";

import { JSX, useEffect, useRef } from "react";

import { FileType, Report } from "../../types";
import {
  ExpansionState,
  useFocus,
  useFocusDispatch,
} from "../providers/FocusContext";
import {
  useSelection,
  useSelectionDispatch,
} from "../providers/SelectionContext";

import "./CodeHighlight";
interface CodeRenderParams {
  content: string;
  syntax: FileType;
  reports: Report[];
}

// TODO:
//     scroll to primary or top unless explicitely clicked on line
// FIXME: multiple reports per line....?
export function CodeRender({ content, syntax, reports }: CodeRenderParams) {
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

  const highlight = (text: string) =>
    syntax !== "text"
      ? hljs.highlight(text, {
          language: syntax,
        }).value
      : text;

  reports.forEach((report) => {
    const loc = report.primarySourceLocation;
    const lines = content.slice(loc.offset, loc.offset + loc.len).split("\n");
    const nLines = lines.length;

    let offset = loc.offset;

    const partial =
      report.primarySourceLocation.col !== 1 ||
      content.charAt(loc.offset + loc.len) !== "\n";

    for (let line = loc.line; line < loc.line + nLines; line++) {
      if (!reportsByLine[line]) {
        reportsByLine[line] = [];
      }

      const lineContent = lines[line - loc.line];

      let start: number | undefined = undefined;
      let end: number | undefined = undefined;

      if (partial) {
        if (line === loc.line && loc.col !== 1) {
          start = loc.col - 1;
        } else {
          const trimmed = lineContent.trimStart();
          start = lineContent.length - trimmed.length || 1;
        }

        if (line === loc.line + nLines - 1) {
          end = loc.offset + loc.len - offset + start;
        } else {
          end = start + lineContent.length;
        }
      }

      reportsByLine[line].push({ report, start, end });
      offset += lineContent.length + 1;
    }
  });

  const code: JSX.Element[] = [];

  code.push(
    <span key="line-0" className="source-file-empty-line-number">
      {" "}
    </span>,
    <span
      key="0"
      className="source-file-line-content source-file-empty-line"
    ></span>,
  );

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

    let highlighted: string | undefined = undefined;

    // FIXME:
    if (relevant?.start || relevant?.end) {
      const begin = relevant?.start
        ? highlight(line.slice(0, relevant.start - 1))
        : "";
      const mid =
        `<span class="source-report-text-highlight source-report-text-highlight-${relevant?.report.severity}"` +
        `data-report="${relevant?.report.id}">${highlight(
          line.slice(relevant?.start ? relevant.start - 1 : 0, relevant?.end),
        )}</span>`;
      const end = relevant?.end ? highlight(line.slice(relevant.end)) : "";

      highlighted = begin + mid + end;
    } else {
      highlighted = highlight(line);
    }

    const click = relevant
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
              key: `${relevant?.report.severity}-${relevant?.report.primarySourceLocation.source?.file.name}`,
              value: ExpansionState.Expanded,
            });
          }

          selectionDispatch({
            type: "click",
            id: id,
            scroll: selected !== id ? "report" : "none",
          });
        }
      : undefined;

    const mouseEnter = relevant
      ? () =>
          selectionDispatch({
            type: "mouseEnter",
            id: id,
            scroll: "none",
          })
      : undefined;

    const mouseLeave = relevant
      ? () =>
          selectionDispatch({
            type: "mouseLeave",
            id: id,
            scroll: "none",
          })
      : undefined;

    const numberClass = cx(
      "source-file-line-number",
      report?.length && "source-report",
      relevant?.report.severity && `source-report-${relevant.report.severity}`,
      (isHovered || isSelected) && "selected",
    );
    const contentClass = cx(
      "source-file-line-content",
      report?.length && "source-report",
      relevant?.report.severity && `source-report-${relevant.report.severity}`,
      isHovered && "hovered",
      isSelected && "selected",
    );

    code.push(
      <span key={`line-${n}`} className={numberClass}></span>,
      <span
        key={n}
        className={contentClass}
        data-report={id}
        ref={
          relevant &&
          selected === id &&
          relevant?.report.primarySourceLocation.line === n
            ? focus
            : undefined
        }
        dangerouslySetInnerHTML={{ __html: highlighted }}
        onClick={click}
        onMouseEnter={mouseEnter}
        onMouseLeave={mouseLeave}
      ></span>,
    );
  });

  return (
    <div className="source-file-render">
      <pre className="code">{code}</pre>
    </div>
  );
}
