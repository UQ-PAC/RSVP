import { SourceFile } from "./SourceFile";
import { Report } from "../ReportsContext";

export interface SourceFileInfo {
  filename: string;
  serverId: string;
  contents: string;
}
interface SourceFileViewerParams {
  sources: SourceFileInfo[];
  reports?: Report[];
}

export function SourceFileViewer({ sources, reports }: SourceFileViewerParams) {
  return (
    <div className="source-files-container">
      {sources.map((source) => (
        <SourceFile
          key={source.serverId}
          filename={source.filename}
          content={source.contents}
          reports={(reports ?? [])
            .filter((report) => report.source.file === source.serverId)
            .sort((a: Report, b: Report) => a.source.offset - b.source.offset)}
        />
      ))}
    </div>
  );
}
