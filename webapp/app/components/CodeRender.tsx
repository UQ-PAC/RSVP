"use client";

import cx from "classnames";

import { useEffect, useRef } from "react";
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

export function CodeRender({
  content,
  reports,
  openReportsDrawer,
}: CodeRenderParams) {
  const { selected, hovered, scroll } = useSelection();
  const dispatch = useSelectionDispatch();

  const element = useRef<HTMLDivElement>(null);

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

  return (
    <div className={`source-file-render ${robotoMono.className}`}>
      {begin}
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
      ))}
    </div>
  );
}
