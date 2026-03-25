export type filetype = "policy" | "schema" | "entity" | "invariant";

export interface SourceFileInfo {
  filename: string;
  serverId: string;
  contents: string;
}

export interface SourceLoc {
  file: string;
  source?: SourceFileInfo;
  offset: number;
  len: number;
  line: number;
  col: number;
}

export type ReportSeverity = "info" | "warn" | "err";

export interface Report {
  id: string;
  primarySourceLocation: SourceLoc;
  sourceLocations: SourceLoc[];
  severity: ReportSeverity;
  message: string;
  messageDetail?: string;
}

export const sortReports = (reports: Report[]) =>
  reports.sort(
    (a: Report, b: Report) =>
      a.primarySourceLocation.offset - b.primarySourceLocation.offset,
  );
