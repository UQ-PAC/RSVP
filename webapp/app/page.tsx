import { Drawer } from "./components/Drawer";
import { Header } from "./components/header/Header";
import { ApplicationStateProvider } from "./components/providers/ApplicationStateProvider";
import { ReportViewer } from "./components/reports/ReportViewer";
import { SourceFileViewer } from "./components/sources/SourceFileViewer";
import { FileUploader } from "./components/upload/FileUploader";

export default function Home() {
  return (
    <div className="app">
      <ApplicationStateProvider>
        <Header heading="RSVP" subheading="Policy Verification" />
        <div className="app-content">
          <Drawer title="Policies" side="left">
            <FileUploader />
          </Drawer>
          <SourceFileViewer />
          <Drawer title="Reports" side="right">
            <ReportViewer />
          </Drawer>
        </div>
      </ApplicationStateProvider>
    </div>
  );
}
