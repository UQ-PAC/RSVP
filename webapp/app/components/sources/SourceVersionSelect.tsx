"use client";

import {
  faCodeCompare,
  faFileCircleMinus,
  faFileCirclePlus,
  IconDefinition,
} from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { JSX } from "react";
import { Tabs } from "../shared/tabs/Tabs";

interface VersionSpec {
  id: string;
  name: string;
}

interface Selection {
  original: string;
  updated?: string;
}

interface SourceVersionSelectParams {
  versions: VersionSpec[];
  selected: Selection;
  expanded: boolean;
  tabIcon: IconDefinition;
  setVersion: (version?: string, compare?: string) => void;
  onClickHeader: () => void;
  onMouseOverHeader: () => void;
  onMouseOutHeader: () => void;
  children?: JSX.Element | JSX.Element[];
}

export function SourceVersionSelect({
  versions,
  selected,
  expanded,
  tabIcon,
  setVersion,
  onClickHeader,
  onMouseOverHeader,
  onMouseOutHeader,
  children,
}: SourceVersionSelectParams) {
  const originalIndex = versions.findIndex(
    (version) => version.id === selected.original,
  );
  const updatedIndex = versions.findIndex(
    (version) => version.id === selected.updated,
  );

  return (
    <div className="source-file-tabs">
      <div
        className="top-row"
        onClick={onClickHeader}
        onMouseOver={onMouseOverHeader}
        onMouseOut={onMouseOutHeader}
      >
        <Tabs
          options={versions.length + 1}
          overrideOption={!!selected.updated ? versions.length : originalIndex}
          onSelect={(version) => {
            if (version === versions.length) {
              setVersion(versions.at(-2)?.id, versions.at(-1)?.id);
            } else {
              setVersion(versions.at(version)?.id);
            }
          }}
          tabContent={(version) => {
            if (version === versions.length) {
              return (
                <>
                  <FontAwesomeIcon
                    className="source-file-icon"
                    icon={faCodeCompare}
                  />
                  Compare
                </>
              );
            } else {
              return (
                <>
                  <FontAwesomeIcon
                    className="source-file-icon"
                    icon={tabIcon}
                  />
                  <span className="source-file-version-indicator">
                    {`Version ${version + 1}`}
                  </span>
                  {versions[version]?.name}
                </>
              );
            }
          }}
          tabTheme={(version) =>
            version === versions.length ? "dark" : "light"
          }
        />
        {children}
      </div>
      {!!selected.updated && expanded && (
        <div className="bottom-row">
          {!!selected.updated && (
            <div className="source-comparison-tabs">
              <FontAwesomeIcon
                className="source-compare-icon"
                icon={faFileCircleMinus}
              />
              <Tabs
                options={versions.length - 1}
                overrideOption={originalIndex}
                onSelect={(version) => {
                  setVersion(
                    versions[version].id,
                    versions[
                      updatedIndex > version
                        ? updatedIndex
                        : versions.length - 1
                    ].id,
                  );
                }}
                tabContent={(version) => (
                  <>
                    <FontAwesomeIcon
                      className="source-file-icon"
                      icon={tabIcon}
                    />
                    <span className="source-file-version-indicator">
                      {`Version ${version + 1}`}
                    </span>
                    {versions[version]?.name}
                  </>
                )}
              />
              <FontAwesomeIcon
                className="source-compare-icon"
                icon={faFileCirclePlus}
              />
              <Tabs
                options={versions.length - originalIndex - 1}
                overrideOption={updatedIndex - originalIndex - 1}
                onSelect={(version) => {
                  setVersion(
                    selected.original,
                    versions[version + originalIndex + 1].id,
                  );
                }}
                tabContent={(version) => (
                  <>
                    <FontAwesomeIcon
                      className="source-file-icon"
                      icon={tabIcon}
                    />
                    <span className="source-file-version-indicator">
                      {`Version ${originalIndex + version + 2}`}
                    </span>
                    {versions[originalIndex + version + 1].name}
                  </>
                )}
              />
            </div>
          )}
        </div>
      )}
    </div>
  );
}
