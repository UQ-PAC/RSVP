import { SourceFile } from "./SourceFile";

export interface SourceFileInfo {
  filename: string;
  serverId: string;
  contents: string;
}

interface SourceFileViewerParams {
  sources: SourceFileInfo[];
}

export function SourceFileViewer({ sources }: SourceFileViewerParams) {
  return (
    <div className="source-files-container">
      {sources.map((source) => (
        <SourceFile
          key={source.serverId}
          filename={source.filename}
          reports={[]}
        >
          {source.contents}
        </SourceFile>
      ))}
    </div>
  );
}
