import { Report, SourceLoc } from "../types";

export interface ReportLine {
  report: Report;
  loc: SourceLoc;
  start?: number;
  end?: number;
}

export interface ReportLineDict {
  [line: number]: ReportLine[];
}
