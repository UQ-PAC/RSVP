"use client";

import { SourceFile } from "./SourceFile";
import { Report, VerificationFile } from "../../types";
import { useVerification } from "../providers/VerificationContext";
import { Fallback } from "./Fallback";

import "./sources.css";
import {
  ExpansionState,
  useFocus,
  useFocusDispatch,
} from "../providers/FocusContext";
import { ToggleAll } from "../shared/ToggleAll";
import { diff } from "@/app/requests";

export function SourceFileViewer() {
  const verificationContext = useVerification();

  const { "source-file": focus } = useFocus();
  const focusDispatch = useFocusDispatch();

  const toggleAll = (group: string, expand: ExpansionState) => {
    const sources = verificationContext[group]?.files;

    sources?.forEach((source) => {
      // If a version is already expanded, leave it as-is, otherwise expand original file
      if (
        expand == ExpansionState.Expanded &&
        source.versions.length &&
        focus[source.original.file.name] === ExpansionState.Collapsed
      ) {
        if (
          !source.versions.some(
            (file) => focus[file.file.name] === ExpansionState.Expanded,
          )
        ) {
          focusDispatch({
            type: "source-file",
            key: source.original.file.name,
            value: expand,
          });
        }
      } else {
        focusDispatch({
          type: "source-file",
          key: source.original.file.name,
          value: expand,
        });
      }
    });
  };

  const filterReports = (
    source: VerificationFile,
    reports?: Promise<Report[]>,
  ): Promise<Report[]> =>
    reports?.then((reports) =>
      reports.filter(
        // TODO: multiple locations
        (report) => report.primarySourceLocation.source === source,
      ),
    ) ?? Promise.resolve([]);

  const groups = Object.entries(verificationContext);

  const getDiffs = async (
    group: string,
    original: VerificationFile,
    updated: VerificationFile,
  ): Promise<string> => {
    const diffs = verificationContext[group]?.diffs;

    if (!diffs) {
      console.error("Error retrieving diff for group: " + group);
      return "";
    }

    return original.resolved
      .then((original) => original.serverId)
      .then((originalId) =>
        updated.resolved
          .then((updated) => updated.serverId)
          .then((updatedId) => {
            let unifiedDiff = diffs[originalId]?.[updatedId];

            if (!unifiedDiff) {
              unifiedDiff = diff(
                { id: originalId, name: original.file.name },
                { id: updatedId, name: updated.file.name },
              );

              if (!diffs[originalId]) {
                diffs[originalId] = {};
              }
              diffs[originalId][updatedId] = unifiedDiff;
            }

            return unifiedDiff;
          }),
      );
  };

  return (
    <div className="source-files-container">
      {groups.map(([name, group], i) => (
        <div key={i} className="source-files-analysis-group">
          <span className="source-files-analysis-group-header">
            <h2 className="source-files-analysis-group-title">{name}</h2>
            <ToggleAll
              name="source-file-analysis-group"
              toggle={(expand) => toggleAll(name, expand)}
            />
          </span>
          {group.files.map((source, i) => (
            <SourceFile
              key={i}
              source={source}
              reports={filterReports(source.original, group.reports)}
              getDiff={(original, updated) => getDiffs(name, original, updated)}
            />
          ))}
          {!group.files.length && (
            <Fallback instruction="Upload Cedar policy and schema files" />
          )}
        </div>
      ))}
      {!groups.length && <Fallback instruction="Create a policy set" />}
    </div>
  );
}
