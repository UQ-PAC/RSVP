"use client";

import { SourceFile } from "./SourceFile";
import { Report, useSelection } from "../SelectionContext";
import { useRef } from "react";

export interface SourceFileInfo {
  filename: string;
  serverId: string;
  contents: string;
}
interface SourceFileViewerParams {
  sources: SourceFileInfo[];
  reports?: Report[];
  openUploadDrawer: () => void;
  openReportsDrawer: () => void;
}

export function SourceFileViewer({
  sources,
  reports,
  openUploadDrawer,
  openReportsDrawer,
}: SourceFileViewerParams) {
  const container = useRef(null);

  // const { scroll } = useSelection();
  // if (scroll === "source") {
  //   console.log("SCROLL SOURCE");
  // }

  return (
    <div ref={container} className="source-files-container">
      {!sources.length && (
        <p className="source-files-instruction">
          <a className="source-files-upload-link" onClick={openUploadDrawer}>
            Upload Cedar policy and schema files
          </a>{" "}
          to run verification.
        </p>
      )}
      {sources.map((source) => (
        <SourceFile
          key={source.serverId}
          filename={source.filename}
          content={source.contents}
          openReportsDrawer={openReportsDrawer}
          reports={(reports ?? [])
            .filter(
              (report) => report.primarySourceLocation.file === source.serverId,
            )
            .sort(
              (a: Report, b: Report) =>
                a.primarySourceLocation.offset - b.primarySourceLocation.offset,
            )}
        />
      ))}
    </div>
  );
}
