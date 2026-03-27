import { createContext, Dispatch, useContext } from "react";

type FocusTarget = "drawer" | "report-group" | "source-file";
type ExpansionMap = { [key: string]: boolean };

interface FocusState {
  [target: string]: ExpansionMap;
}

export const emptyFocus: FocusState = {
  drawer: {
    left: true,
    right: false,
  },
  "report-group": {},
  "source-file": {},
};

interface FocusAction {
  type: FocusTarget;
  key: string;
  value: boolean;
}

export function reducer(context: FocusState, action: FocusAction): FocusState {
  console.log(JSON.stringify(action));

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
