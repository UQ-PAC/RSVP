import { createContext, Dispatch, useContext } from "react";
import { ScrollTarget } from "../types";

interface SelectionState {
  selected?: string;
  hovered?: string;
  loc?: string;
  file?: string;
  group?: string;
  scroll?: ScrollTarget;
  highlighted?: { file: string; start: number; end: number };
}

export const emptySelection = {};

type SelectionAction = SelectionState;

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
