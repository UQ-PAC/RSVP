export type FileType =
  | "cedar"
  | "cedarschema"
  | "entities"
  | "invariant"
  | "text";
export type ReportSeverity = "info" | "warn" | "err";

export interface SourceLoc {
  file: string;
  source?: VerificationFile;
  offset: number;
  len: number;
  line: number;
  col: number;
}

export interface Report {
  id: string;
  primarySourceLocation: SourceLoc;
  sourceLocations: SourceLoc[];
  severity: ReportSeverity;
  message: string;
  messageDetail?: string;
}

export type VersionedPolicy = string[];

export interface VerificationRequest {
  policyFiles: VersionedPolicy[];
  schemas: string[];
  entities: string[];
  invariants: string[];
}

export interface UploadedFile {
  serverId: string;
  content: Promise<string>;
}

export interface VerificationFile {
  file: File;
  filetype: FileType;
  resolved: Promise<UploadedFile>;
}

export interface VersionedFile {
  original: VerificationFile;
  versions: VerificationFile[];
}
