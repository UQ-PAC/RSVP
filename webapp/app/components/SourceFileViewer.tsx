import { SourceFile, Report } from "./SourceFile";

export interface SourceFileInfo {
  filename: string;
  serverId: string;
  contents: string;
}

interface SourceFileViewerParams {
  sources: SourceFileInfo[];
  reports: Report[];
  selected?: string;
  active?: string;
  onselect: (id: string) => void;
  onenter: (id: string) => void;
  onleave: (id: string) => void;
}

export function SourceFileViewer({
  sources,
  reports,
  selected,
  active,
  onselect,
  onenter,
  onleave,
}: SourceFileViewerParams) {
  return (
    <div className="source-files-container">
      {sources.map((source) => (
        <SourceFile
          key={source.serverId}
          filename={source.filename}
          content={source.contents}
          reports={reports
            ?.filter((report) => report.source.file === source.serverId)
            .sort((a: Report, b: Report) => a.source.offset - b.source.offset)}
          selected={selected}
          active={active}
          onclick={onselect}
          onenter={onenter}
          onleave={onleave}
        />
      ))}
    </div>
  );
}
