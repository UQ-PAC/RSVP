import { createContext, Dispatch, useContext } from "react";

interface SelectionState {
  selected?: string;
  hovered?: string;
  loc?: string;
  file?: string;
  scroll?: "source" | "report" | "source-file" | "none";
}

export const emptySelection = {};

interface SelectionAction {
  hovered?: string;
  selected?: string;
  scroll: "source" | "report" | "source-file" | "none";
  file?: string;
  loc?: string;
}

export function selectionReducer(
  context: SelectionState,
  action: SelectionAction,
): SelectionState {
  return {
    ...context,
    ...action,
  };
}

export const SelectionContext = createContext<SelectionState>(emptySelection);
export const SelectionDispatchContext = createContext<
  Dispatch<SelectionAction>
>(() => {});

export function useSelection() {
  return useContext(SelectionContext);
}

export function useSelectionDispatch() {
  return useContext(SelectionDispatchContext);
}
