"use client";

import { faFileLines } from "@fortawesome/free-regular-svg-icons";
import { faCodeCompare } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import cx from "classnames";

interface SourceVersionSelectParams {
  versions: number[];
  versionNames: string[];
  selectedVersion: number;
  selectedCompare?: number;
  set: (version: number, compare?: number) => void;
}

export function SourceVersionSelect({
  versions,
  versionNames,
  selectedVersion,
  selectedCompare,
  set,
}: SourceVersionSelectParams) {
  return (
    <div className="source-file-versions">
      {versions.map((version, i) => {
        const className = cx(
          "source-file-version",
          version === 1 && "source-file-original",
          version === selectedVersion &&
            selectedCompare === undefined &&
            "selected",
        );
        return (
          <div
            key={version}
            className={className}
            onClick={(e) => {
              set(version);
              e.stopPropagation();
            }}
          >
            <FontAwesomeIcon className="source-file-icon" icon={faFileLines} />
            {versionNames[i]}
          </div>
        );
      })}
      {versions.flatMap((version, i) => {
        return versions.slice(i + 1).map((compare) => {
          const className = cx(
            "source-file-version",
            "source-file-version-comparison",
            version === selectedVersion &&
              compare === selectedCompare &&
              "selected",
          );
          return (
            <div
              key={`${version} | ${compare}`}
              className={className}
              onClick={(e) => {
                set(version, compare);
                e.stopPropagation();
              }}
            >
              <FontAwesomeIcon
                className="source-file-icon"
                icon={faCodeCompare}
              />
              <span>
                {version} | {compare}
              </span>
            </div>
          );
        });
      })}
    </div>
  );
}
