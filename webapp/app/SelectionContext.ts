import { createContext, Dispatch, useContext } from "react";

interface SourceLoc {
  file: string;
  offset: number;
  len: number;
}

export interface Report {
  id: string;
  source: SourceLoc;
  severity: "info" | "warn" | "err";
  message: string;
}

interface SelectionState {
  selected?: string;
  hovered?: string;
  scroll?: "source" | "report" | "none";
}

interface Action {
  type: "click" | "mouseEnter" | "mouseLeave" | "reports";
  id?: string;
  source?: "source" | "report";
}

export function reducer(
  context: SelectionState,
  action: Action,
): SelectionState {
  switch (action.type) {
    case "click":
      const selectionChanged = action.id !== context.selected;
      return {
        ...context,
        selected: !selectionChanged ? "" : action.id,
        scroll: selectionChanged
          ? action.source === "source"
            ? "report"
            : "source"
          : "none",
      };
    case "mouseEnter":
      return {
        ...context,
        hovered: action.id,
      };
    case "mouseLeave":
      if (action.id === context.hovered) {
        return {
          ...context,
          hovered: "",
        };
      }
  }
  return context;
}

export const SelectionContext = createContext<SelectionState>({});
export const SelectionDispatchContext = createContext<Dispatch<Action>>(
  (_: Action) => {},
);

export function useSelection() {
  return useContext(SelectionContext);
}

export function useSelectionDispatch() {
  return useContext(SelectionDispatchContext);
}
