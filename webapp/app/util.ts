import { FileType, Report, VerificationFile, VersionedFile } from "./types";

function compareReports(a: Report, b: Report): number {
  return a.primarySourceLocation.offset - b.primarySourceLocation.offset;
}

export function sortReports(reports: Report[]): Report[] {
  return reports.sort(compareReports);
}

function compareSources(a: VerificationFile, b: VerificationFile): number {
  return a.file.name.localeCompare(b.file.name);
}

export function sortSources(sources: VersionedFile[]): VersionedFile[] {
  return sources.sort((a: VersionedFile, b: VersionedFile) =>
    compareSources(a.original, b.original),
  );
}

export function getFileType(file: File): FileType {
  if (file.name.endsWith(".cedar")) {
    return "cedar";
  } else if (file.name.endsWith(".cedarschema")) {
    return "cedarschema";
  } else if (file.name.endsWith(".json")) {
    return "entities";
  } else if (file.name.endsWith(".invariant")) {
    return "invariant";
  } else {
    return "text";
  }
}
