"use client";

import cx from "classnames";
import { Roboto_Mono } from "next/font/google";

import { Report, VerificationFile } from "../../lib/types";

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
              />
            ) : (
              <CodeLine key={n} line={line} syntax={file.filetype} />
            ),
          )}
      </pre>
    </div>
  );
}
