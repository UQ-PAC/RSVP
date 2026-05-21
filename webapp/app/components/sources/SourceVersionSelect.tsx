"use client";

import {
  faCodeCompare,
  faFileCircleMinus,
  faFileCirclePlus,
  IconDefinition,
} from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import cx from "classnames";
import { JSX, useEffect, useRef } from "react";

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

  const selectedTopTab = useRef<HTMLDivElement>(null);
  const selectedOriginalTab = useRef<HTMLDivElement>(null);
  const selectedUpdateTab = useRef<HTMLDivElement>(null);

  const horizontalScroll = (e, elem: HTMLDivElement | null) => {
    e.preventDefault();
    e.stopPropagation();
    elem?.scrollTo({
      left: elem.scrollLeft + e.deltaY * 3,
      behavior: "smooth",
    });
  };

  useEffect(() => {
    // On selection, if selected tabs are hidden then scroll them into view
    if (selectedUpdate) {
      scrollTabIfNeeded(selectedTopTab.current, topRow.current);
      scrollTabIfNeeded(
        selectedOriginalTab.current,
        comparisonOriginal.current,
      );
      scrollTabIfNeeded(selectedUpdateTab.current, comparisonUpdated.current);
    } else {
      scrollTabIfNeeded(selectedTopTab.current, topRow.current);
    }
  }, [selectedOriginal, selectedUpdate]);

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
          {versions.map((version, i) => {
            const isSelected =
              version === selectedOriginal && selectedUpdate === undefined;
            return (
              <div
                key={version}
                ref={isSelected ? selectedTopTab : undefined}
                className={cx("source-file-version", isSelected && "selected")}
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
            );
          })}
          <div
            key="compare"
            ref={selectedUpdate !== undefined ? selectedTopTab : undefined}
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
                className="source-file-versions source-comparison-original"
                ref={comparisonOriginal}
                onWheel={(e) => horizontalScroll(e, comparisonOriginal.current)}
              >
                {versions.slice(0, -1).map((version, i) => {
                  const isSelected =
                    version === selectedOriginal &&
                    selectedUpdate !== undefined;
                  return (
                    <div
                      key={version}
                      ref={isSelected ? selectedOriginalTab : undefined}
                      className={cx(
                        "source-file-version",
                        isSelected && "selected",
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
                className="source-file-versions source-comparison-updated"
                ref={comparisonUpdated}
                onWheel={(e) => horizontalScroll(e, comparisonUpdated.current)}
              >
                {versions.slice(originalIndex + 1).map((version, i) => {
                  const isSelected = selectedUpdate === version;
                  return (
                    <div
                      key={version}
                      ref={isSelected ? selectedUpdateTab : undefined}
                      className={cx(
                        "source-file-version",
                        isSelected && "selected",
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

function scrollTabIfNeeded(
  tab: HTMLDivElement | null,
  container: HTMLDivElement | null,
): void {
  if (tab && container) {
    const tabOffset = tab.offsetLeft - container.offsetLeft;
    if (tabOffset + tab.offsetWidth * 0.95 < container.scrollLeft) {
      tab.scrollIntoView({
        block: "center",
        behavior: "instant",
      });
    } else if (
      tabOffset >
      container.scrollLeft + container.offsetWidth * 0.98
    ) {
      tab.scrollIntoView({
        block: "center",
        behavior: "instant",
      });
    }
  }
}
