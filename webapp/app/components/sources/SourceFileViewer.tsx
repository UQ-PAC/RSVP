"use client";

import { SourceFile } from "./SourceFile";
import { Report, VerificationFile } from "../../types";
import {
  ExpansionState,
  useFocus,
  useFocusDispatch,
} from "../providers/FocusContext";
import { useVerification } from "../providers/VerificationContext";
import { getFileType } from "@/app/util";

export function SourceFileViewer() {
  // useEffect(() => {
  //   hljs.debugMode();
  //   hljs.registerLanguage("cedar", () => CedarHighlight);
  //   hljs.registerLanguage("invariant", () => InvariantHighlight);
  //   // hljs.registerLanguage("entities", () => EntitiesHighlight);
  // }, []);

  const { drawer: drawerFocus } = useFocus();
  const focusDispatch = useFocusDispatch();

  const verificationContext = useVerification();

  const filterReports = (
    source: VerificationFile,
    reports?: Promise<Report[]>,
  ): { source: VerificationFile; reports: Promise<Report[]> } => ({
    source,
    reports:
      reports?.then((reports) =>
        reports.filter(
          // TODO: multiple locations
          (report) => report.primarySourceLocation.source === source,
        ),
      ) ?? Promise.resolve([]),
  });

  const sources = Object.entries(verificationContext).reduce<
    { source: VerificationFile; reports: Promise<Report[]> }[]
  >((all, [, group]) => {
    const filterGroup = (source) => filterReports(source, group.reports);

    // FIXME:??? get from map of all? want ordered
    return [
      ...all,
      ...group.cedar.map(filterGroup),
      ...group.cedarschema.map(filterGroup),
      ...group.entities.map(filterGroup),
      ...group.invariant.map(filterGroup),
    ];
  }, []);

  return (
    <div className="source-files-container">
      {!sources.length && (
        <p className="source-files-instruction">
          <a
            className="source-files-upload-link"
            onClick={() => {
              if (drawerFocus["left"] == ExpansionState.Collapsed) {
                focusDispatch({
                  type: "drawer",
                  key: "left",
                  value: ExpansionState.Expanded,
                });
                focusDispatch({
                  type: "drawer",
                  key: "right",
                  value: ExpansionState.Collapsed,
                });
              }
            }}
          >
            Upload Cedar policy and schema files
          </a>{" "}
          to run verification.
        </p>
      )}
      {sources.map(({ source, reports }, i) => (
        <SourceFile
          key={i}
          filename={source.file.name}
          filetype={getFileType(source.file) ?? "cedar"}
          content={source.resolved.then((uploaded) => uploaded.content)}
          reports={reports}
        />
      ))}
    </div>
  );
}
