"use client";

import { faFileLines } from "@fortawesome/free-regular-svg-icons";
import { faCodeCompare } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import cx from "classnames";
import { JSX } from "react";

interface SourceVersionSelectParams {
  versions: number[];
  versionNames: string[];
  selectedVersion: number;
  selectedCompare?: number;
  expanded: boolean;
  set: (version: number, compare?: number) => void;
  children?: JSX.Element | JSX.Element[];
}

export function SourceVersionSelect({
  versions,
  versionNames,
  selectedVersion,
  selectedCompare,
  expanded,
  set,
  children,
}: SourceVersionSelectParams) {
  return (
    <div className="source-file-tabs">
      <div className="top-row">
        <div className="source-file-versions">
          {versions.map((version, i) => (
            <div
              key={version}
              className={cx(
                "source-file-version",
                version === 1 && "source-file-original",
                version === selectedVersion &&
                  selectedCompare === undefined &&
                  "selected",
              )}
              onClick={(e) => {
                set(version);
                e.stopPropagation();
              }}
            >
              <FontAwesomeIcon
                className="source-file-icon"
                icon={faFileLines}
              />
              {versionNames[i]}
            </div>
          ))}
          <div
            key="compare"
            className={cx(
              "source-file-version",
              "source-file-compare",
              selectedCompare !== undefined && "selected",
            )}
            onClick={(e) => {
              set(1, 2);
              e.stopPropagation();
            }}
          >
            <FontAwesomeIcon
              className="source-file-icon"
              icon={faCodeCompare}
            />
            Compare
          </div>
        </div>
        {children}
      </div>
      {expanded && (
        <div className="bottom-row">
          {selectedCompare !== undefined && (
            <div className="source-comparison-tabs">
              <div className="source-comparison-original">
                <FontAwesomeIcon
                  className="source-compare-icon"
                  icon={faCodeCompare}
                />
                {versions.slice(0, -1).map((version, i) => {
                  return (
                    <div
                      key={version}
                      className={cx(
                        "source-file-version",
                        version === 1 && "source-file-original",
                        version === selectedVersion &&
                          selectedCompare !== undefined &&
                          "selected",
                      )}
                      onClick={(e) => {
                        set(version, version + 1);
                        e.stopPropagation();
                      }}
                    >
                      <FontAwesomeIcon
                        className="source-file-icon"
                        icon={faFileLines}
                      />
                      {versionNames[i]}
                    </div>
                  );
                })}
              </div>
              <div className="source-comparison-updated">
                <FontAwesomeIcon
                  className="source-compare-icon"
                  icon={faCodeCompare}
                />
                {versions.slice(selectedVersion).map((version, i) => {
                  return (
                    <div
                      key={version}
                      className={cx(
                        "source-file-version",
                        selectedCompare === version && "selected",
                      )}
                      onClick={(e) => {
                        set(selectedVersion, version);
                        e.stopPropagation();
                      }}
                    >
                      <FontAwesomeIcon
                        className="source-file-icon"
                        icon={faFileLines}
                      />
                      {versionNames[selectedVersion + i]}
                    </div>
                  );
                })}
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
