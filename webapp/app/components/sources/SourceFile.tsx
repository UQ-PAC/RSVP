"use client";

import {
  faSquareMinus,
  faSquarePlus,
} from "@fortawesome/free-regular-svg-icons";
import { faFileLines } from "@fortawesome/free-regular-svg-icons/faFileLines";
import { faCodeCompare } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import cx from "classnames";
import { useEffect, useRef, useState } from "react";
import { Report, VerificationFile, VersionedFile } from "../../types";
import {
  ExpansionState,
  useFocus,
  useFocusDispatch,
} from "../providers/FocusContext";
import { useSelectionDispatch } from "../providers/SelectionContext";
import { CodeRender } from "./CodeRender";
import { DiffRender } from "./DiffRender";
import { SourceVersionSelect } from "./SourceVersionSelect";

type IdentifiedFile = { file: VerificationFile; id: string };

interface SourceFileParams {
  source: VersionedFile;
  reports: Promise<Report[]>;
  getDiff: (
    original: VerificationFile,
    updated: VerificationFile,
  ) => Promise<string>;
  setFocus: (original: string, updated?: string) => void;
}

export function SourceFile({
  source,
  reports,
  getDiff,
  setFocus,
}: SourceFileParams) {
  const {
    "source-file": { expansions },
  } = useFocus();
  const focusDispatch = useFocusDispatch();
  const selectionDispatch = useSelectionDispatch();

  const [focusIds, setFocusIds] = useState<string[]>([]);
  const [versionIds, setVersionIds] = useState<string[]>([]);
  const [files, setFiles] = useState<IdentifiedFile[]>([]);

  const [filename, setFilename] = useState("");
  const [code, setCode] = useState("");

  const [resolvedReports, setResolvedReports] = useState<Report[]>([]);

  const [original, setOriginal] = useState<IdentifiedFile>();
  const [updated, setUpdated] = useState<IdentifiedFile>();

  const focus = useRef<Promise<string>>(null);

  const expanded = focusIds.some(
    (id) => expansions[id] === ExpansionState.Expanded,
  );

  // Focus initialisation
  useEffect(() => {
    if (!focus.current) {
      focus.current = source.original.resolved
        .then((uploaded) => uploaded.serverId)
        .then((id) => {
          setFocus(id);
          return id;
        });
    }

    source.original.resolved.then((uploaded) => {
      Promise.all(
        source.versions.map((version) =>
          version.resolved.then((resolved) => ({
            unresolved: version,
            resolved,
          })),
        ),
      ).then((versions) => {
        const files: IdentifiedFile[] = [
          { file: source.original, id: uploaded.serverId },
          ...versions.map((version) => ({
            file: version.unresolved,
            id: version.resolved.serverId,
          })),
        ];
        const ids: string[] = [...files.map(({ id }) => id)];
        const allIds: string[] = [
          ...ids,
          ...versions.map(
            ({ resolved }) => `${uploaded.serverId}${resolved.serverId}`,
          ),
          ...versions
            .slice(0, -1)
            .map((version, i) =>
              versions
                .slice(i)
                .map(
                  ({ resolved }) =>
                    `${version.resolved.serverId}${resolved.serverId}`,
                ),
            )
            .flat(),
        ];

        setVersionIds(ids);
        setFocusIds(allIds);
        setFiles(files);
        focusDispatch({
          type: "register",
          target: "source-file",
          register: [uploaded.serverId, allIds],
        });
      });
    });
  }, [source.original, source.versions, focusDispatch, setFocus]);

  // Set focused files based on focus state
  useEffect(() => {
    focus.current =
      focus.current
        ?.then((focusId) => {
          const focused = focusIds.find(
            (id) => expansions[id] === ExpansionState.Expanded,
          );

          if (focused && focused !== focusId) {
            return focused;
          }

          return focusId;
        })
        .then((focusId) => {
          const single = files.find(({ id }) => id === focusId);

          if (single) {
            setOriginal({
              file: single.file,
              id: single.id,
            });
            setUpdated(undefined);

            single.file.resolved
              .then((uploaded) => uploaded.content)
              .then((content) => {
                setFilename(single?.file.file.name);
                setCode(content);
              });
          } else {
            const comparison = files.reduce<{
              original?: IdentifiedFile;
              updated?: IdentifiedFile;
            }>(({ original, updated }, { file, id: originalId }, i) => {
              const match = files
                .slice(i)
                .find(
                  ({ id: updatedId }) => originalId + updatedId === focusId,
                );

              if (match) {
                return {
                  original: { file, id: originalId },
                  updated: {
                    file: match.file,
                    id: match.id,
                  },
                };
              }

              return { original, updated };
            }, {});

            setOriginal(comparison?.original);
            setUpdated(comparison?.updated);
          }

          return focusId;
        }) ?? null;
  }, [expansions, focusIds, files, focusDispatch]);

  // Resolve reports
  useEffect(() => {
    reports.then((resolved) => setResolvedReports(resolved));
  }, [reports]);

  const render =
    original && updated ? (
      <DiffRender diff={getDiff(original.file, updated.file)} />
    ) : (
      <CodeRender
        content={code}
        syntax={source.original.filetype}
        reports={resolvedReports.filter(
          (report) => report.primarySourceLocation.source === original?.file,
        )}
      />
    );

  return (
    <div className={cx("source-file", expanded ? "expanded" : "collapsed")}>
      <div
        className="source-file-header"
        onClick={() => {
          selectionDispatch({ type: "other", scroll: "none" });
          focus.current?.then((key) => {
            focusDispatch({
              type: "focus",
              target: "source-file",
              focus: {
                key,
                value: expanded
                  ? ExpansionState.Collapsed
                  : ExpansionState.Expanded,
              },
            });
          });
        }}
      >
        {source.versions.length === 0 && (
          <FontAwesomeIcon
            className="source-file-icon"
            icon={original && updated ? faCodeCompare : faFileLines}
          />
        )}
        {source.versions.length === 0 && (
          <h2 className="source-file-name">{filename}</h2>
        )}
        {original && versionIds.length > 1 && (
          <SourceVersionSelect
            versions={versionIds}
            versionNames={[
              source.original.file.name,
              ...source.versions.map((file) => file.file.name),
            ]}
            selectedOriginal={original.id}
            selectedUpdate={updated?.id}
            set={(original, updated) => {
              const id = updated ? original + updated : original;
              focus.current = Promise.resolve(id);
              setFocus(id);
              focusDispatch({
                type: "focus",
                target: "source-file",
                focus: {
                  key: id,
                  value: ExpansionState.Expanded,
                },
              });
            }}
            expanded={expanded}
          >
            <FontAwesomeIcon
              className="source-file-toggle"
              icon={expanded ? faSquareMinus : faSquarePlus}
            />
          </SourceVersionSelect>
        )}

        {source.versions.length === 0 && (
          <FontAwesomeIcon
            className="source-file-toggle"
            icon={expanded ? faSquareMinus : faSquarePlus}
          />
        )}
      </div>
      <div className="source-file-content">{expanded && render}</div>
    </div>
  );
}
