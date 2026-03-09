import React from "react";

interface SourceLoc {
  serverId: string;
  offset: number;
  len: number;
}
export interface Report {
  source: SourceLoc;
  severity: "info" | "warn" | "err";
  message: string;
}
interface SourceFileParams {
  filename: string;
  children: React.ReactNode;
  reports: Report[];
}

export function SourceFile({ filename, children, reports }: SourceFileParams) {
  return (
    <div className="source-file-render">
      <div className="source-file-header">{filename}</div>
      <div className="source-file-contents">{children}</div>
    </div>
  );
}
