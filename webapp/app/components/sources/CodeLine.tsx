import { getHighlightFunction } from "../../lib/sources/util";
import { FileType } from "../../lib/types";

interface CodeLineProps {
  line: string;
  syntax: FileType;
}

export function CodeLine({ line, syntax }: CodeLineProps) {
  const highlight = getHighlightFunction(syntax);

  return (
    <>
      <span className="source-file-line-number" />
      <span
        className="source-file-line-content"
        dangerouslySetInnerHTML={{ __html: highlight(line) }}
      />
    </>
  );
}
