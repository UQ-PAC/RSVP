"use client";

import { useState } from "react";
import { FilePond } from "react-filepond";
import type { FilePondErrorDescription, FilePondFile } from "filepond";
import { SourceFileViewer } from "./components/files/SourceFileViewer";

import { Header } from "./components/Header";

import "filepond/dist/filepond.min.css";
import { ReportViewer } from "./components/reports/ReportViewer";
import { SelectionProvider } from "./components/providers/SelectionProvider";
import { Drawer } from "./components/Drawer";
import { Content } from "./components/Content";
import {
  Report,
  sortReports,
  SourceFileInfo,
  VerificationFileset,
} from "./types";
import { FocusProvider } from "./components/providers/FocusProvider";

export default function Home() {
  const [files, setFiles] = useState<FilePondFile[]>([]);
  const [sources, setSources] = useState<SourceFileInfo[]>([]);
  const [reports, setReports] = useState<Report[] | undefined>(undefined);

  const add = (error: FilePondErrorDescription | null, file: FilePondFile) => {
    if (error) {
      console.error(error.body);
    } else {
      fetch(`/api/file/${file.serverId}`)
        .then((res) => res.text())
        .then((contents) => {
          setSources([
            ...sources,
            { filename: file.filename, serverId: file.serverId, contents },
          ]); // TODO: expand by default!
        });
    }
  };

  const remove = (id: string) => {
    fetch(`/api/file/${id}`, { method: "DELETE" }).then(() => {
      setSources(sources.filter((source) => source.serverId != id));
      setReports(
        reports?.filter((report) => report.primarySourceLocation.file != id),
      );
    });
  };

  const warn = (error: any, file?: FilePondFile, status?: any) =>
    console.warn(JSON.stringify(error));

  const err = (
    error: FilePondErrorDescription,
    file?: FilePondFile,
    status?: any,
  ) => console.error(error.body);

  // Resolve serverIds to actual source files
  const resolveFilenames = (report: Report): Report => ({
    ...report,
    primarySourceLocation: {
      ...report.primarySourceLocation,
      source: sources.find(
        (source) => source.serverId === report.primarySourceLocation.file,
      ),
    },
    sourceLocations: report.sourceLocations.map((sourceLoc) => ({
      ...sourceLoc,
      source: sources.find((source) => source.serverId === sourceLoc.file),
    })),
  });

  const verify = () => {
    const body = JSON.stringify(
      sources.map(
        (source) =>
          ({
            policyFiles: [[{ version: "v1", id: source.serverId }]],
            schemas: [],
            entities: [],
            invariants: [],
          }) as VerificationFileset,
      ),
    );

    console.log(body);

    // TODO: check if response is OK
    return fetch("/api/verify", {
      headers: {
        Accept: "application/json",
        "Content-Type": "application/json",
      },
      method: "POST",
      body,
    })
      .then((res) => res.json())
      .then((reports) => reports.map(resolveFilenames))
      .then(sortReports)
      .then(setReports)
      .catch((err) => {
        console.error(err);
      });
  };

  return (
    <div className="app">
      <FocusProvider>
        <SelectionProvider>
          <Header
            heading="RSVP"
            subheading="Policy Verification"
            verify={verify}
          />
          <Content>
            <Drawer title="Policy sets" side="left">
              <FilePond
                files={files.map((item) => item.file)}
                // onupdatefiles={setFiles}
                // onwarning={warn}
                // onerror={err}
                // onprocessfilerevert={remove}
                // onprocessfile={add}

                onupdatefiles={(files: FilePondFile[]) => {
                  console.log(
                    "onupdatefiles " +
                      JSON.stringify(
                        files.map(
                          (file) => `${file.filename} (${file.serverId})`,
                        ),
                      ),
                  );
                  setFiles(files);
                }}
                /** FilePond instance has been created and is ready. */
                oninit={() => console.log("oninit")}
                /**
                 * FilePond instance throws a warning. For instance
                 * when the maximum amount of files has been reached.
                 * Optionally receives file if error is related to a
                 * file object.
                 */
                onwarning={(error: any, file?: FilePondFile, status?: any) =>
                  console.log("onwarning")
                }
                /**
                 * FilePond instance throws an error. Optionally receives
                 * file if error is related to a file object.
                 */
                onerror={(
                  error: FilePondErrorDescription,
                  file?: FilePondFile,
                  status?: any,
                ) => console.log("onerror")}
                /** Started file load. */
                onaddfilestart={(file: FilePondFile) =>
                  console.log("onaddfilestart")
                }
                /** Made progress loading a file. */
                onaddfileprogress={(file: FilePondFile, progress: number) =>
                  console.log("onaddfileprogress")
                }
                /** If no error, file has been successfully loaded. */
                onaddfile={(
                  error: FilePondErrorDescription | null,
                  file: FilePondFile,
                ) => console.log("onaddfile")}
                /** Started processing a file. */
                onprocessfilestart={(file: FilePondFile) =>
                  console.log("onprocessfilestart")
                }
                /** Made progress processing a file. */
                onprocessfileprogress={(file: FilePondFile, progress: number) =>
                  console.log("onprocessfileprogress")
                }
                /** Aborted processing of a file. */
                onprocessfileabort={(file: FilePondFile) =>
                  console.log("onprocessfileabort")
                }
                /** Processing of a file has been reverted. */
                onprocessfilerevert={(file: FilePondFile) => {
                  console.log(
                    `onprocessfilerevert: ${file.filename} (${file.serverId})`,
                  );
                  remove(file.serverId);
                }}
                /** If no error, Processing of a file has been completed. */
                onprocessfile={(
                  error: FilePondErrorDescription | null,
                  file: FilePondFile,
                ) => {
                  console.log(
                    `onprocessfile: ${file.filename} (${file.serverId})`,
                  );
                  add(error, file);
                }}
                /** Called when all files in the list have been processed. */
                onprocessfiles={() => console.log("onprocessfiles")}
                /** File has been removed. */
                onremovefile={(
                  error: FilePondErrorDescription | null,
                  file: FilePondFile,
                ) =>
                  console.log(
                    `onremovefile: ${file.filename} (${file.serverId})`,
                  )
                }
                /**
                 * File has been transformed by the transform plugin or
                 * another plugin subscribing to the prepare_output filter.
                 * It receives the file item and the output data.
                 */
                onpreparefile={(file: FilePondFile, output: any) =>
                  console.log("onpreparefile")
                }
                /* Called when a file is clicked or tapped. **/
                onactivatefile={(file: FilePondFile) =>
                  console.log("onactivatefile")
                }
                /** Called when the files have been reordered */
                onreorderfiles={(files: FilePondFile[]) =>
                  console.log("onreorderfiles")
                }
                allowMultiple={true}
                maxFiles={20}
                server="/api/upload"
                name="document" /* sets the file input name, it's filepond by default */
                labelIdle='Drag & Drop your files or <span class="filepond--label-action">Browse</span>'
              />
            </Drawer>
            <SourceFileViewer sources={sources} reports={reports} />
            <Drawer title="Reports" side="right">
              <ReportViewer reports={reports} />
            </Drawer>
          </Content>
        </SelectionProvider>
      </FocusProvider>
    </div>
  );
}
