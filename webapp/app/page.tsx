import { SourceFileViewer } from "./components/sources/SourceFileViewer";
import { Header } from "./components/header/Header";
import { ReportViewer } from "./components/reports/ReportViewer";
import { Drawer } from "./components/Drawer";
import { ContextProvider } from "./components/providers/ContextProvider";
import { FileUploader } from "./components/upload/FileUploader";

export default function Home() {
  return (
    <div className="app">
      <ContextProvider>
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
      </ContextProvider>
    </div>
  );
}
