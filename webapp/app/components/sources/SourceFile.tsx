"use client";

import { useEffect, useState } from "react";
import { ExpansionStatus } from "../../lib/context/ExpansionContext";
import { Report, VersionedFile } from "../../lib/types";
import { SingleSourceFile } from "./SingleSourceFile";
import { SourceFileFallback } from "./SourceFileFallback";
import { ResolvedFile, VersionedSourceFile } from "./VersionedSourceFile";

interface SourceFileParams {
  source: VersionedFile;
  reports: Promise<Report[]>;
  setExpansionCallback: (toggle: (status: ExpansionStatus) => void) => void;
}

export function SourceFile({
  source,
  reports,
  setExpansionCallback,
}: SourceFileParams) {
  const [resolvedReports, setResolvedReports] = useState<Report[]>([]);
  const [files, setFiles] = useState<ResolvedFile[]>([]);

  // Resolve files
  useEffect(() => {
    source.original.resolved.then((uploaded) => {
      const resolved = {
        file: source.original,
        resolved: uploaded,
      };

      if (source.versions.length) {
        Promise.all(
          source.versions.map((version) =>
            version.resolved.then((resolved) => ({
              file: version,
              resolved,
            })),
          ),
        ).then((versions) => {
          setFiles([resolved, ...versions]);
        });
      } else {
        setFiles([resolved]);
      }
    });
  }, [source.original, source.versions]);

  // Resolve reports
  useEffect(() => {
    reports.then((resolved) => setResolvedReports(resolved));
  }, [reports]);

  if (!files || !files.length) {
    return <SourceFileFallback file={source.original} />;
  } else if (files.length === 1) {
    return (
      <SingleSourceFile
        file={source.original}
        uploaded={files[0].resolved}
        reports={resolvedReports}
        setExpansionCallback={setExpansionCallback}
      />
    );
  } else {
    return (
      <VersionedSourceFile
        files={files}
        reports={resolvedReports}
        setExpansionCallback={setExpansionCallback}
      />
    );
  }
}
