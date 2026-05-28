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

export const SelectionContext = createContext<SelectionState>(emptySelection);
export const SelectionDispatchContext = createContext<
  Dispatch<SelectionAction>
>(() => {});

/* istanbul ignore next */
export function useSelection() {
  return useContext(SelectionContext);
}

/* istanbul ignore next */
export function useSelectionDispatch() {
  return useContext(SelectionDispatchContext);
}

export function selectionReducer(
  context: SelectionState,
  action: SelectionAction,
): SelectionState {
  const change = Object.keys(action).some(
    (key) => action[key] !== context[key],
  );

  if (!change) {
    return context;
  }

  return {
    ...context,
    ...action,
  };
}
