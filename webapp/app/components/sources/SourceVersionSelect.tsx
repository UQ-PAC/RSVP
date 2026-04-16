"use client";

import cx from "classnames";

interface SourceVersionSelectParams {
  versions: number[];
  selectedVersion: number;
  selectedCompare?: number;
  set: (version: number, compare?: number) => void;
}

export function SourceVersionSelect({
  versions,
  selectedVersion,
  selectedCompare,
  set,
}: SourceVersionSelectParams) {
  return (
    <div className="source-file-versions">
      {versions.map((version) => {
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
            {version}
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
