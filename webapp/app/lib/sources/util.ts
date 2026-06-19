import hljs from "highlight.js";
import { ReportLine, ReportLineDict } from "../../lib/sources/types";
import { Report, SourceLoc, VerificationFile } from "../../lib/types";

export function sortReportLinesByPrecedence(reports: ReportLine[]): {
  reportsByPrecedence: ReportLine[];
  mostSevere?: ReportLine;
  mostRelevant?: ReportLine;
} {
  const reportsByPrecedence = [...reports].sort(compareReportLinesByPrecedence);

  const mostSevere = reports.reduce<ReportLine | undefined>(
    (result, current) => {
      switch (result?.report.severity) {
        case "err":
          return result;
        case "warn":
          return current.report.severity === "err" ? current : result;
        default:
          return current;
      }
    },
    undefined,
  );

  const mostRelevant = reportsByPrecedence.at(0);

  return { reportsByPrecedence, mostSevere, mostRelevant };
}

function compareReportLinesByPrecedence(a: ReportLine, b: ReportLine): number {
  if (a.loc.offset === b.loc.offset) {
    return a.loc.len - b.loc.len;
  }

  return b.loc.offset - a.loc.offset;
}

export function groupReportsBySourceLine(
  file: VerificationFile,
  content: string,
  reports?: Report[],
): { reportsByLine: ReportLineDict; lines: string[] } {
  const reportsByLine: ReportLineDict = {};
  const lines = content.split("\n");

  reports?.forEach((report) => {
    report.sourceLocations.forEach(({ location }) =>
      processReportLinesForSourceLoc(
        file,
        report,
        location,
        lines,
        content,
        reportsByLine,
      ),
    );
  });

  return { reportsByLine, lines };
}

function processReportLinesForSourceLoc(
  file: VerificationFile,
  report: Report,
  loc: SourceLoc,
  lines: string[],
  content: string,
  reportsByLine: ReportLineDict,
): void {
  if (loc.source !== file) return;
  if (!loc.startLoc || !loc.endLoc) return;

  const reportLines = lines.slice(loc.startLoc.line - 1, loc.endLoc.line);

  if (!reportLines.length) return;

  // Check whether the report location covers the entire line
  // so that it can be rendered as a block
  const startOffset =
    !!`${reportLines[0].substring(0, loc.startLoc.column - 1)}`.trim().length;
  const endOffset =
    loc.offset + loc.len < content.length &&
    content.charAt(loc.offset + loc.len) !== "\n" &&
    !(
      file.filetype === "entities" &&
      content.substring(loc.offset + loc.len, loc.offset + loc.len + 2) ===
        ",\n"
    );

  // Track offset since report start
  let offset = loc.offset - loc.startLoc.column + 1;

  // let offset = startOffset;
  const partial = startOffset || endOffset;

  for (let line = loc.startLoc.line; line <= loc.endLoc.line; line++) {
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
        start = lineContent.length - trimmed.length;
      }

      if (line === loc.endLoc.line) {
        end = loc.offset + loc.len - offset;
      } else {
        end = lineContent.length;
      }
    }

    reportsByLine[line].push({ report, loc, start, end });

    // Increment offset by line length plus newline
    offset += lineContent.length + 1;
  }
}

export function getHighlightFunction(syntax: string) {
  if (syntax === "text") {
    return (text: string) => text;
  }

  return (text: string) => hljs.highlight(text, { language: syntax }).value;
}
