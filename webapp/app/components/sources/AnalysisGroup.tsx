"use client";

import { Fallback } from "./Fallback";
import { SourceFile } from "./SourceFile";

import { useEffect, useRef } from "react";
import { useAnalysisGroup } from "../../lib/context/AnalysisGroupContext";
import { ExpansionStatus } from "../../lib/context/ExpansionContext";
import { useSelection } from "../../lib/context/SelectionContext";
import { ToggleAll } from "../shared/ToggleAll";
import "./sources.css";

interface ExpansionToggleMap {
  [index: string]: (status: ExpansionStatus) => void;
}

export function AnalysisGroup() {
  const { name, files, reports } = useAnalysisGroup();
  const { scroll, group: selectedGroup } = useSelection();

  const expansionFunctions = useRef<ExpansionToggleMap>({});
  const container = useRef<HTMLDivElement>(null);

  // Scroll selected policy into view if relevant
  useEffect(() => {
    if (container.current && scroll === "group" && selectedGroup === name) {
      container.current.scrollIntoView({
        block: "start",
        inline: "start",
        behavior: "smooth",
      });
    }
  }, [scroll, selectedGroup, name]);

  // Invoke all expansion callbacks from children
  const toggleAll = (status: ExpansionStatus) => {
    Object.values(expansionFunctions.current).forEach((toggle) =>
      toggle(status),
    );
  };

  return (
    <div
      ref={container}
      className="source-files-analysis-group"
      data-testid="source-analysis-group"
    >
      {files.length > 0 && (
        <span className="source-files-analysis-group-header">
          <h2 className="source-files-analysis-group-title">{name}</h2>
          <ToggleAll
            name="source-file-analysis-group"
            toggle={(expand) => toggleAll(expand)}
          />
        </span>
      )}
      {files.map((source) => {
        const key =
          source.original.filename +
          source.original.file.lastModified +
          source.original.file.size;
        return (
          <SourceFile
            key={key}
            source={source}
            reports={reports ?? Promise.resolve([])}
            setExpansionCallback={(toggle: (status: ExpansionStatus) => void) =>
              (expansionFunctions.current[key] = toggle)
            }
          />
        );
      })}
      {!files.length && (
        <Fallback
          instruction="Upload Cedar policy, schema and entities files"
          target={name}
        />
      )}
    </div>
  );
}
