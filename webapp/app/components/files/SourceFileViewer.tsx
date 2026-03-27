"use client";

import { SourceFile } from "./SourceFile";
import { Report, SourceFileInfo } from "../../types";
import { useEffect } from "react";
import hljs from "highlight.js";
import { CedarHighlight } from "./CedarHighlight";
import { useFocus, useFocusDispatch } from "../providers/FocusContext";
interface SourceFileViewerParams {
  sources: SourceFileInfo[];
  reports?: Report[];
}

export function SourceFileViewer({ sources, reports }: SourceFileViewerParams) {
  useEffect(() => {
    hljs.debugMode();
    hljs.registerLanguage("cedar", () => CedarHighlight);
    hljs.configure({ ignoreUnescapedHTML: true });
  }, []);

  const { drawer: drawerFocus } = useFocus();
  const focusDispatch = useFocusDispatch();

  return (
    <div className="source-files-container">
      {!sources.length && (
        <p className="source-files-instruction">
          <a
            className="source-files-upload-link"
            onClick={() => {
              if (!drawerFocus["left"]) {
                focusDispatch({ type: "drawer", key: "left", value: true });
                focusDispatch({ type: "drawer", key: "right", value: false });
              }
            }}
          >
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
          reports={(reports ?? []).filter(
            (report) => report.primarySourceLocation.file === source.serverId,
          )}
        />
      ))}
    </div>
  );
}
