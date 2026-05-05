import {
  FileType,
  Report,
  SourceLoc,
  VerificationFile,
  VersionedFile,
  VersionedFileList,
} from "./types";

function compareSources(a: VerificationFile, b: VerificationFile): number {
  return a.filename.localeCompare(b.filename);
}

export function sortSources(sources: VersionedFile[]): VersionedFile[] {
  return sources.sort((a: VersionedFile, b: VersionedFile) =>
    compareSources(a.original, b.original),
  );
}

function compareReports(a: Report, b: Report): number {
  if (!a.sourceLocations.length && !b.sourceLocations.length) {
    return 0;
  } else if (!a.sourceLocations.length) {
    return 1;
  } else if (!b.sourceLocations.length) {
    return -1;
  } else {
    return (
      a.sourceLocations[0].location.offset -
      b.sourceLocations[0].location.offset
    );
  }
}

export function sortReports(reports: Report[]): Report[] {
  return reports.sort(compareReports);
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

export function getSourceIdentifier(loc: SourceLoc): string {
  return `${loc.file}:${loc.offset}:${loc.len}`;
}

export function getSourceStr(loc: SourceLoc): string {
  return `Line ${loc.startLoc?.line}, column ${loc.startLoc?.column}`;
}

export function checkAnalysisGroup(files: VersionedFileList): {
  hasPolicy: boolean;
  hasSchema: boolean;
  hasEntities: boolean;
  error: boolean;
} {
  const hasPolicy = files.some((file) => file.original.filetype === "cedar");
  const hasSchema = files.some(
    (file) => file.original.filetype === "cedarschema",
  );
  const hasEntities = files.some(
    (file) => file.original.filetype === "entities",
  );

  const error = !hasPolicy || !hasSchema || !hasEntities;

  return { hasPolicy, hasSchema, hasEntities, error };
}
