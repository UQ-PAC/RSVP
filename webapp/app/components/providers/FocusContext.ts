import { createContext, Dispatch, useContext } from "react";

type FocusTarget = "drawer" | "report-group" | "source-file";

export enum ExpansionState {
  Expanded = 0,
  Collapsed = 1,
}

type ExpansionMap = { [key: string]: ExpansionState };
interface FocusState {
  [target: string]: ExpansionMap;
}

export const emptyFocus: FocusState = {
  drawer: {
    left: ExpansionState.Expanded,
    right: ExpansionState.Collapsed,
  },
  "report-group": {},
  "source-file": {},
};

interface FocusAction {
  type: FocusTarget;
  key: string;
  value: ExpansionState;
}

export function focusReducer(
  context: FocusState,
  action: FocusAction,
): FocusState {
  const updatedContext = { ...context };
  const updatedTarget = { ...context[action.type] };

  updatedTarget[action.key] = action.value;
  updatedContext[action.type] = updatedTarget;

  return updatedContext;
}

export const FocusContext = createContext<FocusState>(emptyFocus);

export const FocusDispatchContext = createContext<Dispatch<FocusAction>>(
  () => {},
);

export function useFocus() {
  return useContext(FocusContext);
}

export function useFocusDispatch() {
  return useContext(FocusDispatchContext);
}
