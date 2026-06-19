import cx from "classnames";
import { Roboto_Mono } from "next/font/google";

import { Report, VerificationFile } from "../../lib/types";

import { useEffect, useRef, useState } from "react";
import {
  useSelection,
  useSelectionDispatch,
} from "../../lib/context/SelectionContext";
import { ReportLineDict } from "../../lib/sources/types";
import { groupReportsBySourceLine } from "../../lib/sources/util";
import "./CodeHighlight";
import { CodeLine } from "./CodeLine";
import { HighlightedCodeLine } from "./HighlightedCodeLine";

const robotoMono = Roboto_Mono({
  subsets: ["latin"],
});
interface CodeRenderParams {
  file: VerificationFile;
  content: Promise<string>;
  reports?: Report[];
}

type HighlightFunc = (line: number) => boolean;

export function CodeRender({ file, content, reports }: CodeRenderParams) {
  const timeoutRef = useRef<NodeJS.Timeout>(null);

  const { scroll, highlighted } = useSelection();
  const selectionDispatch = useSelectionDispatch();

  // Line rendering
  const [lines, setLines] = useState<string[]>([]);
  const [reportsByLine, setReportsByLine] = useState<ReportLineDict>({});

  useEffect(() => {
    content.then((code) => {
      const { lines, reportsByLine } = groupReportsBySourceLine(
        file,
        code,
        reports,
      );
      setLines(lines);
      setReportsByLine(reportsByLine);
    });
  }, [file, content, reports]);

  // Animated highlight rendering
  const [temporaryHighlight, setTemporaryHighlight] = useState<HighlightFunc>(
    () => () => false,
  );

  useEffect(() => {
    if (scroll === "source") {
      if (highlighted) {
        file.resolved
          .then(({ serverId }) => serverId)
          .then((id) => {
            if (id === highlighted.file) {
              setTemporaryHighlight(
                () => (n: number) =>
                  n >= highlighted.start && n <= highlighted.end,
              );
              // Clear any existing timeout
              if (timeoutRef.current) {
                clearTimeout(timeoutRef.current);
              }
              timeoutRef.current = setTimeout(() => {
                setTemporaryHighlight(() => () => false);
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

  // Clear any timeout on unmount
  useEffect(() => {
    return () => {
      if (timeoutRef.current) {
        clearTimeout(timeoutRef.current);
      }
    };
  }, []);

  const id = file.resolved.then(({ serverId }) => serverId);

  return (
    <div className={cx("source-file-render", robotoMono.className)}>
      <pre className="code">
        <span className="source-file-empty-line-number"> </span>
        <span className="source-file-line-content source-file-empty-line">
          {" "}
        </span>
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
                syntax={file.filetype}
                reports={reports}
                temporaryHighlight={temporaryHighlight}
              >
                {line}
              </HighlightedCodeLine>
            ) : (
              <CodeLine
                key={n}
                n={n}
                file={id}
                syntax={file.filetype}
                temporaryHighlight={temporaryHighlight}
              >
                {line}
              </CodeLine>
            ),
          )}
      </pre>
    </div>
  );
}
