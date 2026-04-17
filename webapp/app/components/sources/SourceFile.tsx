"use client";

import {
  faSquareMinus,
  faSquarePlus,
} from "@fortawesome/free-regular-svg-icons";
import { faFileLines } from "@fortawesome/free-regular-svg-icons/faFileLines";
import { faCodeCompare } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import cx from "classnames";
import { Roboto_Mono } from "next/font/google";
import { useEffect, useState } from "react";
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

const robotoMono = Roboto_Mono({
  subsets: ["latin"],
});

interface SourceFileParams {
  source: VersionedFile;
  reports: Promise<Report[]>;
  getDiff: (
    original: VerificationFile,
    updated: VerificationFile,
  ) => Promise<string>;
}

export function SourceFile({ source, reports, getDiff }: SourceFileParams) {
  const [code, setCode] = useState("");
  const [resolvedReports, setResolvedReports] = useState<Report[]>([]);

  const [filename, setFilename] = useState(source.original.file.name);
  const [version, setVersion] = useState(1);
  const [compare, setCompare] = useState<number | undefined>();

  const { "source-file": focus } = useFocus();
  const focusDispatch = useFocusDispatch();
  const selectionDispatch = useSelectionDispatch();

  const expanded = !focus[filename];

  useEffect(() => {
    const updateSingleFile = async (file: VerificationFile) =>
      file.resolved
        .then((uploaded) => uploaded.content)
        .then((content) => {
          setFilename(file.file.name);
          setCode(content);
          focusDispatch({
            type: "source-file",
            key: file.file.name,
            value: ExpansionState.Expanded,
          });
        });

    if (compare !== undefined) {
    } else if (version === 1) {
      updateSingleFile(source.original);
    } else {
      updateSingleFile(source.versions[version - 2]);
    }
  }, [version, compare, source.original, source.versions, focusDispatch]);

  useEffect(() => {
    reports.then((resolved) => setResolvedReports(resolved));
  }, [reports]);

  let render = (
    <CodeRender
      content={code}
      syntax={source.original.filetype}
      reports={resolvedReports}
    />
  );

  if (compare) {
    const original =
      version === 1 ? source.original : source.versions[version - 2];
    const comparison = source.versions[compare - 2];

    render = (
      <DiffRender
        left={original}
        right={comparison}
        diff={getDiff(original, comparison)}
      />
    );
  }

  return (
    <div className={cx("source-file", expanded ? "expanded" : "collapsed")}>
      <div
        className="source-file-header"
        onClick={() => {
          selectionDispatch({ type: "other", scroll: "none" });
          focusDispatch({
            type: "source-file",
            key: filename,
            value: expanded
              ? ExpansionState.Collapsed
              : ExpansionState.Expanded,
          });
        }}
      >
        <FontAwesomeIcon
          className="source-file-icon"
          icon={compare ? faCodeCompare : faFileLines}
        />
        <h2 className="source-file-name">{compare ? "Comparison" : "x"}</h2>
        {source.versions.length > 0 && (
          <SourceVersionSelect
            versions={Array.from(
              { length: source.versions.length + 1 },
              (_, i) => i + 1,
            )}
            selectedVersion={version}
            selectedCompare={compare}
            set={(version: number, compare?: number) => {
              setVersion(version);
              setCompare(compare);
            }}
          />
        )}
        <span className="spacer" />
        <FontAwesomeIcon
          className="source-file-toggle"
          icon={expanded ? faSquareMinus : faSquarePlus}
        />
      </div>
      <div className={cx("source-file-content", robotoMono.className)}>
        {expanded && render}
      </div>
    </div>
  );
}
