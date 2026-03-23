import { ReportItem } from "./ReportItem";
import { Report } from "../ReportsContext";

interface ReportViewerProps {
  reports?: Report[];
}

export function ReportViewer({ reports }: ReportViewerProps) {
  return (
    <div className="reports-container">
      {!reports && (
        <p className="reports-instruction reports-not-run">
          Run verification to see reports
        </p>
      )}
      {reports && !reports.length && (
        <p className="reports-instruction no-reports">No reports to display</p>
      )}
      {reports?.map((report) => (
        <ReportItem key={report.id} report={report} />
      ))}
    </div>
  );
}
