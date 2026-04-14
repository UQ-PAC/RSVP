import { FileType, Report } from "./types";

export function sortReports(reports: Report[]): Report[] {
  return reports.sort(
    (a: Report, b: Report) =>
      a.primarySourceLocation.offset - b.primarySourceLocation.offset,
  );
}

export function getFileType(file: File): FileType | undefined {
  if (file.name.endsWith(".cedar")) {
    return "cedar";
  } else if (file.name.endsWith(".cedarschema")) {
    return "cedarschema";
  } else if (file.name.endsWith(".json")) {
    return "entities";
  } else if (file.name.endsWith(".invariant")) {
    return "invariant";
  }
}
