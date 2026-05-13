"use client";

import { Fallback } from "./Fallback";
import { SourceFile } from "./SourceFile";

import { useEffect, useRef } from "react";
import { useAnalysisGroup } from "../../lib/context/AnalysisGroupContext";
import {
  ExpansionState,
  useFocusDispatch,
} from "../../lib/context/FocusContext";
import { useSelection } from "../../lib/context/SelectionContext";
import { ToggleAll } from "../shared/ToggleAll";
import "./sources.css";

export function AnalysisGroup() {
  const { name, files, reports } = useAnalysisGroup();

  const focusDispatch = useFocusDispatch();
  const { scroll, group: selectedGroup } = useSelection();

  const focusState = useRef<{ [index: string]: string }>({});
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

  const toggleAll = (expand: ExpansionState) => {
    Object.values(focusState.current).forEach((id) => {
      focusDispatch({
        type: "focus",
        target: "source-file",
        focus: { key: id, value: expand },
      });
    });
  };

  return (
    <div ref={container} className="source-files-analysis-group">
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
            setFocus={(original, updated) => {
              focusState.current[key] = updated ? original + updated : original;
            }}
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
