"use client";

import { Report, VerificationFile, VersionedFile } from "../../types";
import { useVerification } from "../providers/VerificationContext";
import { Fallback } from "./Fallback";
import { SourceFile } from "./SourceFile";

import { useRef } from "react";
import { diff } from "../../requests";
import { ExpansionState, useFocusDispatch } from "../providers/FocusContext";
import { ToggleAll } from "../shared/ToggleAll";
import "./sources.css";

type SourceFocusState = { [group: string]: { [index: string]: string } };

export function SourceFileViewer() {
  const verificationContext = useVerification();
  const groups = Object.entries(verificationContext);

  const focusDispatch = useFocusDispatch();
  const focusState = useRef<SourceFocusState>({});

  const toggleAll = (group: string, expand: ExpansionState) => {
    if (focusState.current[group]) {
      Object.values(focusState.current[group]).forEach((id) => {
        focusDispatch({
          type: "focus",
          target: "source-file",
          focus: { key: id, value: expand },
        });
      });
    }
  };

  const filterReports = (
    source: VersionedFile,
    reports?: Promise<Report[]>,
  ): Promise<Report[]> =>
    reports?.then((reports) =>
      reports.filter(
        // TODO: multiple locations
        (report) =>
          report.primarySourceLocation.source === source.original ||
          source.versions.some(
            (version) => report.primarySourceLocation.source === version,
          ),
      ),
    ) ?? Promise.resolve([]);

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
      {groups.map(([name, group], i) => {
        return (
          <div key={i} className="source-files-analysis-group">
            {group.files.length > 0 && (
              <span className="source-files-analysis-group-header">
                <h2 className="source-files-analysis-group-title">{name}</h2>
                <ToggleAll
                  name="source-file-analysis-group"
                  toggle={(expand) => toggleAll(name, expand)}
                />
              </span>
            )}
            {group.files.map((source) => {
              const key =
                source.original.file.name +
                source.original.file.lastModified +
                source.original.file.size;
              return (
                <SourceFile
                  key={key}
                  source={source}
                  reports={filterReports(source, group.reports)}
                  getDiff={(original, updated) =>
                    getDiffs(name, original, updated)
                  }
                  setFocus={(original, updated) => {
                    if (!focusState.current[name]) {
                      focusState.current[name] = {};
                    }

                    focusState.current[name][key] = updated
                      ? original + updated
                      : original;
                  }}
                />
              );
            })}
            {!group.files.length && (
              <Fallback
                instruction="Upload Cedar policy and schema files"
                target={name}
              />
            )}
          </div>
        );
      })}
      {!groups.length && <Fallback instruction="Create a policy set" />}
    </div>
  );
}
