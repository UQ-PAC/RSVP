"use client";

import cx from "classnames";
import { Roboto_Mono } from "next/font/google";

import { Report, VerificationFile } from "../../lib/types";

import { useEffect, useState } from "react";
import {
  useSelection,
  useSelectionDispatch,
} from "../../lib/context/SelectionContext";
import { sortReportsBySourceLine } from "../../lib/sources/util";
import "./CodeHighlight";
import { CodeLine } from "./CodeLine";
import { HighlightedCodeLine } from "./HighlightedCodeLine";

const robotoMono = Roboto_Mono({
  subsets: ["latin"],
});
interface CodeRenderParams {
  file: VerificationFile;
  content: string;
  reports?: Report[];
}

export function CodeRender({ file, content, reports }: CodeRenderParams) {
  const { lines, reportsByLine } = sortReportsBySourceLine(
    file,
    content,
    reports,
  );

  const { scroll, highlighted } = useSelection();
  const selectionDispatch = useSelectionDispatch();

  const [temporaryHighlight, setTemporaryHighlight] = useState(false);

  useEffect(() => {
    if (scroll === "source") {
      if (highlighted) {
        file.resolved
          .then(({ serverId }) => serverId)
          .then((id) => {
            if (id === highlighted.file) {
              setTemporaryHighlight(true);
              setTimeout(() => {
                setTemporaryHighlight(false);
                selectionDispatch({
                  scroll: "none",
                  highlighted: undefined,
                });
              }, 500);
            }
          });
      }
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [file, highlighted, scroll]);

  const id = file.resolved.then(({ serverId }) => serverId);

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
          .map(({ line, reports, n }) =>
            reports?.length ? (
              <HighlightedCodeLine
                key={n}
                n={n}
                line={line}
                syntax={file.filetype}
                reports={reports}
                temporaryHighlight={
                  temporaryHighlight &&
                  !!highlighted &&
                  n >= highlighted.start &&
                  n <= highlighted.end
                }
              />
            ) : (
              <CodeLine
                key={n}
                n={n}
                file={id}
                line={line}
                syntax={file.filetype}
                temporaryHighlight={
                  temporaryHighlight &&
                  !!highlighted &&
                  n >= highlighted.start &&
                  n <= highlighted.end
                }
              />
            ),
          )}
      </pre>
    </div>
  );
}
