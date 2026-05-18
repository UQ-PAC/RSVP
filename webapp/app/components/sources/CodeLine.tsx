"use client";

import cx from "classnames";
import { useEffect, useRef } from "react";
import { useSelection } from "../../lib/context/SelectionContext";
import { getHighlightFunction } from "../../lib/sources/util";
import { FileType } from "../../lib/types";

interface CodeLineProps {
  file: Promise<string>;
  n: number;
  line: string;
  syntax: FileType;
  temporaryHighlight: boolean;
}

export function CodeLine({
  file,
  n,
  line,
  syntax,
  temporaryHighlight,
}: CodeLineProps) {
  const highlight = getHighlightFunction(syntax);

  const { scroll, loc, highlighted } = useSelection();

  // Scroll selected policy into view
  const focus = useRef<HTMLSpanElement>(null);
  useEffect(() => {
    if (focus.current && scroll === "source") {
      if (loc === `${file}:${n}`) {
        focus.current.scrollIntoView({
          block: "center",
          inline: "center",
          behavior: "smooth",
        });
      }
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [scroll, loc]);

  return (
    <>
      <span className="source-file-line-number" />
      <span
        ref={focus}
        className={cx(
          "source-file-line-content",
          "source-line-no-report",
          temporaryHighlight && "highlighted",
        )}
        dangerouslySetInnerHTML={{ __html: highlight(line) }}
      />
    </>
  );
}
