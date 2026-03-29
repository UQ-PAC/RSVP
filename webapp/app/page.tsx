"use client";

import { useState } from "react";
import type { FilePondErrorDescription, FilePondFile } from "filepond";
import { SourceFileViewer } from "./components/sources/SourceFileViewer";

import { Header } from "./components/Header";

import "filepond/dist/filepond.min.css";
import { ReportViewer } from "./components/reports/ReportViewer";
import { Drawer } from "./components/Drawer";
import { Content } from "./components/Content";

import { ContextProvider } from "./components/providers/ContextProvider";
import { FileUploader } from "./components/upload/FileUploader";

export default function Home() {
  return (
    <div className="app">
      <ContextProvider>
        <Header heading="RSVP" subheading="Policy Verification" />
        <Content>
          <Drawer title="Policies" side="left">
            <FileUploader />
          </Drawer>
          <SourceFileViewer />
          <Drawer title="Reports" side="right">
            <ReportViewer />
          </Drawer>
        </Content>
      </ContextProvider>
    </div>
  );
}
