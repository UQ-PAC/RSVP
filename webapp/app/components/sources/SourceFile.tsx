"use client";

import {
  faSquareMinus as regularMinus,
  faSquarePlus as regularPlus,
} from "@fortawesome/free-regular-svg-icons";

import {
  faSquareMinus as solidMinus,
  faSquarePlus as solidPlus,
} from "@fortawesome/free-solid-svg-icons";

import { faCodeCompare } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import cx from "classnames";
import { useEffect, useRef, useState } from "react";
import {
  ExpansionState,
  useFocus,
  useFocusDispatch,
} from "../../lib/context/FocusContext";
import {
  useSelection,
  useSelectionDispatch,
} from "../../lib/context/SelectionContext";
import { Report, VerificationFile, VersionedFile } from "../../lib/types";
import { getFileIcon } from "../../lib/util";
import { CodeRender } from "./CodeRender";
import { DiffRender } from "./DiffRender";
import { SourceVersionSelect } from "./SourceVersionSelect";

type IdentifiedFile = { file: VerificationFile; id: string };

interface SourceFileParams {
  source: VersionedFile;
  reports: Promise<Report[]>;
  setFocus: (original: string, updated?: string) => void;
}

export function SourceFile({ source, reports, setFocus }: SourceFileParams) {
  const {
    "source-file": { expansions },
  } = useFocus();
  const focusDispatch = useFocusDispatch();
  const { scroll, file: focusedFileId } = useSelection();
  const selectionDispatch = useSelectionDispatch();

  const file = useRef<HTMLDivElement>(null);

  const [focusIds, setFocusIds] = useState<string[]>([]);
  const [versionIds, setVersionIds] = useState<string[]>([]);
  const [files, setFiles] = useState<IdentifiedFile[]>([]);

  const [filename, setFilename] = useState(source.original.file.name);
  const [code, setCode] = useState("");

  const [resolvedReports, setResolvedReports] = useState<Report[]>([]);

  const [original, setOriginal] = useState<IdentifiedFile>();
  const [updated, setUpdated] = useState<IdentifiedFile>();

  const focus = useRef<Promise<string>>(null);

  const expanded = focusIds.some(
    (id) => expansions[id] === ExpansionState.Expanded,
  );

  const [toggleHover, setToggleHover] = useState(false);

  const toggleIcon = (
    <FontAwesomeIcon
      className="source-file-toggle"
      icon={
        expanded
          ? toggleHover
            ? solidMinus
            : regularMinus
          : toggleHover
            ? solidPlus
            : regularPlus
      }
    />
  );

  // Focus initialisation
  useEffect(() => {
    // Initially focus on most recent file
    if (!focus.current) {
      const init =
        source.versions?.[source.versions.length - 1] ?? source.original;

      focus.current = init.resolved
        .then((uploaded) => uploaded.serverId)
        .then((id) => {
          setFocus(id);
          return id;
        });
    }

    // Initialise focus state IDs for this file and all versions
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

  // Set focused files based on current focus state
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
                setFilename(single?.file.filename);
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

  // Scroll selected policy into view if relevant
  useEffect(() => {
    focus.current?.then((id) => {
      if (file.current && scroll === "file" && id === focusedFileId) {
        file.current.scrollIntoView({
          block: "start",
          inline: "start",
          behavior: "smooth",
        });
      }
    });
  }, [scroll, focusedFileId]);

  // Resolve reports
  useEffect(() => {
    reports.then((resolved) => setResolvedReports(resolved));
  }, [reports]);

  const render =
    original && updated ? (
      <DiffRender
        original={original.file}
        updated={updated.file}
        originalId={original.id}
        updatedId={updated.id}
      />
    ) : (
      <CodeRender
        file={original?.file ?? source.original}
        content={code}
        reports={resolvedReports?.filter(
          (report) =>
            report.sourceLocations.some(
              (loc) => loc.location.source === source.original,
            ) ||
            source.versions.some((version) =>
              report.sourceLocations.some(
                (loc) => loc.location.source === version,
              ),
            ),
        )}
      />
    );

  const fileIcon = getFileIcon(original?.file.filetype);

  return (
    <div
      ref={file}
      className={cx("source-file", expanded ? "expanded" : "collapsed")}
    >
      <div
        className="source-file-header"
        onClick={() => {
          selectionDispatch({ scroll: "none" });
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
        onMouseEnter={() => setToggleHover(true)}
        onMouseLeave={() => setToggleHover(false)}
      >
        {source.versions.length === 0 && (
          <FontAwesomeIcon
            className="source-file-icon"
            icon={original && updated ? faCodeCompare : fileIcon}
          />
        )}
        {source.versions.length === 0 && (
          <h2 className="source-file-name">{filename}</h2>
        )}
        {original && versionIds.length > 1 && (
          <SourceVersionSelect
            versions={versionIds}
            versionNames={[
              source.original.filename,
              ...source.versions.map((file) => file.filename),
            ]}
            selectedOriginal={original.id}
            selectedUpdate={updated?.id}
            icon={fileIcon}
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
            {toggleIcon}
          </SourceVersionSelect>
        )}

        {source.versions.length === 0 && toggleIcon}
      </div>
      <div className="source-file-content">{expanded && render}</div>
    </div>
  );
}
