"use client";

import {
  faCodeCompare,
  faFileCircleMinus,
  faFileCirclePlus,
  IconDefinition,
} from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import cx from "classnames";
import { JSX, useRef } from "react";

interface SourceVersionSelectParams {
  versions: string[];
  versionNames: string[];
  selectedOriginal: string;
  selectedUpdate?: string;
  expanded: boolean;
  set: (version: string, compare?: string) => void;
  icon: IconDefinition;
  children?: JSX.Element | JSX.Element[];
  mouseOver: () => void;
  mouseOut: () => void;
  click: () => void;
}

export function SourceVersionSelect({
  versions,
  versionNames,
  selectedOriginal,
  selectedUpdate,
  expanded,
  set,
  icon,
  children,
  click,
  mouseOver,
  mouseOut,
}: SourceVersionSelectParams) {
  const originalIndex = versions.indexOf(selectedOriginal);
  const updateIndex = selectedUpdate
    ? versions.indexOf(selectedUpdate)
    : originalIndex + 1;

  const topRow = useRef<HTMLDivElement>(null);
  const comparisonOriginal = useRef<HTMLDivElement>(null);
  const comparisonUpdated = useRef<HTMLDivElement>(null);

  const horizontalScroll = (e, elem: HTMLDivElement | null) => {
    e.preventDefault();
    e.stopPropagation();
    elem?.scrollTo({
      left: elem.scrollLeft + e.deltaY * 3,
      behavior: "smooth",
    });
  };

  return (
    <div className="source-file-tabs">
      <div
        className="top-row"
        onClick={click}
        onMouseOver={mouseOver}
        onMouseOut={mouseOut}
      >
        <div
          className="source-file-versions"
          ref={topRow}
          onWheel={(e) => horizontalScroll(e, topRow.current)}
        >
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
                e.stopPropagation();
                set(version);
              }}
              onMouseOver={(e) => {
                e.stopPropagation();
              }}
            >
              <FontAwesomeIcon className="source-file-icon" icon={icon} />
              <span className="source-file-version-indicator">
                {`Version ${i + 1}`}
              </span>
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
              e.stopPropagation();
              set(versions[0], versions[1]);
            }}
            onMouseOver={(e) => {
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
              <div
                className="source-comparison-original"
                ref={comparisonOriginal}
                onWheel={(e) => horizontalScroll(e, comparisonOriginal.current)}
              >
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
                        e.stopPropagation();
                        const index = versions.indexOf(version);
                        set(
                          version,
                          versions[
                            updateIndex > index ? updateIndex : index + 1
                          ],
                        );
                      }}
                    >
                      <FontAwesomeIcon
                        className="source-file-icon"
                        icon={icon}
                      />
                      <span className="source-file-version-indicator">
                        {`Version ${i + 1}`}
                      </span>
                      {versionNames[i]}
                    </div>
                  );
                })}
              </div>
              <FontAwesomeIcon
                className="source-compare-icon"
                icon={faFileCirclePlus}
              />
              <div
                className="source-comparison-updated"
                ref={comparisonUpdated}
                onWheel={(e) => horizontalScroll(e, comparisonUpdated.current)}
              >
                {versions.slice(originalIndex + 1).map((version, i) => {
                  return (
                    <div
                      key={version}
                      className={cx(
                        "source-file-version",
                        selectedUpdate === version && "selected",
                      )}
                      onClick={(e) => {
                        e.stopPropagation();
                        set(selectedOriginal, version);
                      }}
                    >
                      <FontAwesomeIcon
                        className="source-file-icon"
                        icon={icon}
                      />
                      <span className="source-file-version-indicator">
                        {`Version ${originalIndex + i + 2}`}
                      </span>
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
