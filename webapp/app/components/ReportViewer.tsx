import { ReportItem } from "./ReportItem";
import { Report } from "./SourceFile";

interface ReportViewerParams {
  reports: Report[];
  selected?: string;
  active?: string;
  onselect: (id: string) => void;
  onenter: (id: string) => void;
  onleave: (id: string) => void;
}

export function ReportViewer({
  reports,
  selected,
  active,
  onselect,
  onenter,
  onleave,
}: ReportViewerParams) {
  return (
    <div className="reports-container">
      {reports?.length
        ? reports.map((report) => (
            <ReportItem
              key={report.id}
              report={report}
              selected={selected === report.id}
              active={active === report.id}
              onclick={onselect}
              onactivate={onenter}
              ondeactivate={onleave}
            />
          ))
        : "Run verification to see reports"}
    </div>
  );
}
