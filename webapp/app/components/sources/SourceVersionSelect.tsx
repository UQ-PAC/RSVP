"use client";

import { faFileLines } from "@fortawesome/free-regular-svg-icons";
import {
  faCodeCompare,
  faFileCircleMinus,
  faFileCirclePlus,
} from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import cx from "classnames";
import { JSX } from "react";

interface SourceVersionSelectParams {
  versions: string[];
  versionNames: string[];
  selectedOriginal: string;
  selectedUpdate?: string;
  expanded: boolean;
  set: (version: string, compare?: string) => void;
  children?: JSX.Element | JSX.Element[];
}

export function SourceVersionSelect({
  versions,
  versionNames,
  selectedOriginal,
  selectedUpdate,
  expanded,
  set,
  children,
}: SourceVersionSelectParams) {
  const originalIndex = versions.indexOf(selectedOriginal);
  const updateIndex = selectedUpdate
    ? versions.indexOf(selectedUpdate)
    : originalIndex + 1;

  return (
    <div className="source-file-tabs">
      <div className="top-row">
        <div className="source-file-versions">
          {versions.map((version, i) => (
            <div
              key={version}
              className={cx(
                "source-file-version",
                version === selectedOriginal &&
                  selectedUpdate === undefined &&
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
              selectedUpdate !== undefined && "selected",
            )}
            onClick={(e) => {
              set(versions[0], versions[1]);
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
          {selectedUpdate !== undefined && (
            <div className="source-comparison-tabs">
              <FontAwesomeIcon
                className="source-compare-icon"
                icon={faFileCircleMinus}
              />
              <div className="source-comparison-original">
                {versions.slice(0, -1).map((version, i) => {
                  return (
                    <div
                      key={version}
                      className={cx(
                        "source-file-version",
                        version === selectedOriginal &&
                          selectedUpdate !== undefined &&
                          "selected",
                      )}
                      onClick={(e) => {
                        const index = versions.indexOf(version);
                        set(
                          version,
                          versions[
                            updateIndex > index ? updateIndex : index + 1
                          ],
                        );
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
              <FontAwesomeIcon
                className="source-compare-icon"
                icon={faFileCirclePlus}
              />
              <div className="source-comparison-updated">
                {versions.slice(originalIndex + 1).map((version, i) => {
                  return (
                    <div
                      key={version}
                      className={cx(
                        "source-file-version",
                        selectedUpdate === version && "selected",
                      )}
                      onClick={(e) => {
                        set(selectedOriginal, version);
                        e.stopPropagation();
                      }}
                    >
                      <FontAwesomeIcon
                        className="source-file-icon"
                        icon={faFileLines}
                      />
                      {versionNames[originalIndex + i + 1]}
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
