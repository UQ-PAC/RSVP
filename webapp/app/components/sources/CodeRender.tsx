"use client";

import cx from "classnames";
import { Roboto_Mono } from "next/font/google";
import { useEffect, useMemo, useRef } from "react";

import {
  Report,
  ReportSeverity,
  SourceLoc,
  VerificationFile,
} from "../../types";
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
import { CodeLine, ReportLine } from "./CodeLine";

const robotoMono = Roboto_Mono({
  subsets: ["latin"],
});
interface CodeRenderParams {
  file: VerificationFile;
  content: string;
  reports: Report[];
}

type ReportLineDict = {
  [line: number]: ReportLine[];
};

export function CodeRender({ file, content, reports }: CodeRenderParams) {
  const { selected, hovered, scroll, loc } = useSelection();
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
  }, [scroll, focus, selected, loc]);

  // Only re-calculate if reports or content change (not selection or hover)
  const { lines, reportsByLine } = useMemo(() => {
    const reportsByLine: ReportLineDict = {};
    const lines = content.split("\n");

    const processSourceLoc = (report: Report, loc: SourceLoc) => {
      if (loc.source !== file) return;
      if (!loc.startLoc || !loc.endLoc) return;

      const reportLines = lines.slice(loc.startLoc.line - 1, loc.endLoc.line);
      const nLines = reportLines.length;

      let offset = loc.offset;

      // Check whether the report location covers the entire line
      // so that it can be rendered as a block
      const startOffset =
        !!`${reportLines.at(0)?.substring(0, loc.startLoc.column - 1)}`.trim()
          .length;
      const endOffset =
        content.charAt(loc.offset + loc.len) !== "\n" &&
        !(
          file.filetype === "entities" &&
          content.substring(loc.offset + loc.len, loc.offset + loc.len + 2) ===
            ",\n"
        );
      const partial = startOffset || endOffset;

      for (
        let line = loc.startLoc.line;
        line < loc.startLoc.line + nLines;
        line++
      ) {
        if (!reportsByLine[line]) {
          reportsByLine[line] = [];
        }

        const lineContent = reportLines[line - loc.startLoc.line];

        let start: number | undefined = undefined;
        let end: number | undefined = undefined;

        if (partial) {
          if (line === loc.startLoc.line && loc.startLoc.column !== 1) {
            start = loc.startLoc.column - 1;
          } else {
            const trimmed = lineContent.trimStart();
            start = lineContent.length - trimmed.length || 1;
          }

          if (line === loc.startLoc.line + nLines - 1) {
            end = loc.offset + loc.len - offset + start;
          } else {
            end = start + lineContent.length;
          }
        }

        reportsByLine[line].push({ report, loc, start, end });
        offset += lineContent.length + 1;
      }
    };

    reports.forEach((report) => {
      processSourceLoc(report, report.primarySourceLocation);
      report.sourceLocations.forEach((loc) => processSourceLoc(report, loc));
    });

    return { lines, reportsByLine };
  }, [file, reports, content]);

  const click = (id: string, severity: ReportSeverity, loc: SourceLoc) => {
    if (selected !== id) {
      if (drawerFocus.expansions["right"] !== ExpansionState.Expanded) {
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
          key: `${severity}-${loc.source?.file.name}`,
          value: ExpansionState.Expanded,
        },
      });
    }

    selectionDispatch({
      type: "click",
      id: id,
      scroll: selected !== id ? "report" : "none",
    });
  };

  const mouseEnter = (id: string) =>
    selectionDispatch({
      type: "mouseEnter",
      id: id,
      scroll: "none",
    });

  const mouseLeave = (id: string) =>
    selectionDispatch({
      type: "mouseLeave",
      id: id,
      scroll: "none",
    });

  return (
    <div className={cx("source-file-render", robotoMono.className)}>
      <pre className="code">
        <span className="source-file-empty-line-number"> </span>
        <span className="source-file-line-content source-file-empty-line" />
        {lines
          .map((line, i) => {
            const n = i + 1;
            return { line, reports: reportsByLine[n], n };
          })
          .map(({ line, reports, n }) => (
            <CodeLine
              key={n}
              n={n}
              focus={focus}
              line={line}
              syntax={file.filetype}
              reports={reports}
              selected={selected}
              hovered={hovered}
              selectedLoc={loc}
              onclick={click}
              onenter={mouseEnter}
              onleave={mouseLeave}
            />
          ))}
      </pre>
    </div>
  );
}
