"use client";

import { Fallback } from "./Fallback";
import { SourceFile } from "./SourceFile";

import { useRef } from "react";
import { useAnalysisGroup } from "../../lib/context/AnalysisGroupContext";
import {
  ExpansionState,
  useFocusDispatch,
} from "../../lib/context/FocusContext";
import { ToggleAll } from "../shared/ToggleAll";
import "./sources.css";

export function AnalysisGroup() {
  const group = useAnalysisGroup();

  const focusDispatch = useFocusDispatch();

  const focusState = useRef<{ [index: string]: string }>({});

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
    <div className="source-files-analysis-group">
      {group.files.length > 0 && (
        <span className="source-files-analysis-group-header">
          <h2 className="source-files-analysis-group-title">{group.name}</h2>
          <ToggleAll
            name="source-file-analysis-group"
            toggle={(expand) => toggleAll(expand)}
          />
        </span>
      )}
      {group.files.map((source) => {
        const key =
          source.original.filename +
          source.original.file.lastModified +
          source.original.file.size;
        return (
          <SourceFile
            key={key}
            source={source}
            reports={group.reports ?? Promise.resolve([])}
            setFocus={(original, updated) => {
              focusState.current[key] = updated ? original + updated : original;
            }}
          />
        );
      })}
      {!group.files.length && (
        <Fallback
          instruction="Upload Cedar policy and schema files"
          target={group.name}
        />
      )}
    </div>
  );
}
