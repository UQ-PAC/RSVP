export type FileType =
  | "cedar"
  | "cedarschema"
  | "entities"
  | "invariant"
  | "text";
export type ReportSeverity = "info" | "warn" | "err";
interface LineLoc {
  line: number;
  column: number;
}
export interface SourceLoc {
  file: string;
  source?: VerificationFile;
  offset: number;
  len: number;
  startLoc?: LineLoc;
  endLoc?: LineLoc;
}

export interface Report {
  id: string;
  sourceLocations: {
    message: string;
    location: SourceLoc;
  }[];
  severity: ReportSeverity;
  message: string;
  messageDetail?: string;
}

export type VersionedPolicy = string[];

export interface VerificationRequest {
  policies: VersionedPolicy[];
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
  filename: string;
  filetype: FileType;
  resolved: Promise<UploadedFile>;
}

export interface VersionedFile {
  original: VerificationFile;
  versions: VerificationFile[];
}

export type VerificationFileDict = { [id: string]: VerificationFile };
export type VersionedFileList = VersionedFile[];
export type VersionDict = { [id: string]: string[] };
export type DiffDict = { [id: string]: { [id: string]: string } };
export interface AnalysisGroup {
  name: string;
  files: VersionedFileList;
  byId?: Promise<VerificationFileDict>;
  reports?: Promise<Report[]>;
  diffs: DiffDict;
  impacts?: DiffDict;
  verifyRequested: boolean;
  verifyPending: boolean;
  verifyCompleted?: Promise<boolean>;
}

export type ScrollTarget = "source" | "report" | "file" | "group" | "none";
