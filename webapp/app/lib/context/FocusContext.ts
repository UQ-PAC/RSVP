import { createContext, Dispatch, useContext } from "react";

type FocusTarget = "drawer" | "report-group" | "source-file";

export enum ExpansionState {
  Expanded = 0,
  Collapsed = 1,
}

type ExpansionId = string;
type ExpansionMap = { [key: ExpansionId]: ExpansionState };
type ExpansionGroup = ExpansionId[];
interface FocusState {
  [target: string]: {
    expansions: ExpansionMap;
    groups?: { [key: ExpansionId]: ExpansionGroup };
  };
}

export const emptyFocus: FocusState = {
  drawer: {
    expansions: {
      left: ExpansionState.Expanded,
      right: ExpansionState.Collapsed,
    },
  },
  "report-group": { expansions: {} },
  "source-file": { expansions: {}, groups: {} },
};

interface FocusAction {
  type: "focus" | "register" | "deregister";
  target: FocusTarget;
  focus?: {
    key: string;
    value: ExpansionState;
  };
  unfocus?: string;
  register?: {
    key: ExpansionId;
    options: ExpansionId[];
    default?: ExpansionId;
  };
  deregister?: ExpansionId;
}

export function focusReducer(
  context: FocusState,
  action: FocusAction,
): FocusState {
  switch (action.type) {
    case "focus":
      return doFocus(context, action);
    case "register":
      return doRegister(context, action);
    case "deregister":
      return doDeregister(context, action);
  }
}

function doFocus(context: FocusState, action: FocusAction): FocusState {
  if (!action.focus) {
    return context;
  }

  const updatedContext: FocusState = { ...context };

  const expansions = context[action.target].expansions;
  const groups = context[action.target].groups;
  const updatedTarget = {
    expansions: { ...expansions },
    groups: groups ? { ...groups } : undefined,
  };

  if (action.focus.value === ExpansionState.Expanded) {
    // Collapse all conflicting focus entries
    Object.values(updatedTarget.groups ?? {})
      .find((group) => group?.some((item) => item === action.focus?.key))
      ?.forEach((item) => {
        updatedTarget.expansions[item] = ExpansionState.Collapsed;
      });
  }

  // Expand target
  updatedTarget.expansions[action.focus.key] = action.focus.value;

  updatedContext[action.target] = updatedTarget;

  return updatedContext;
}

function doRegister(context: FocusState, action: FocusAction): FocusState {
  if (!action.register) {
    return context;
  }

  const updatedContext: FocusState = { ...context };

  const expansions = context[action.target].expansions;
  const groups = context[action.target].groups;

  // If a default focus target was provided and no other target in the group
  // is already expanded, then expand the default
  if (
    action.register.default &&
    !action.register.options.some(
      (key) => expansions[key] === ExpansionState.Expanded,
    )
  ) {
    expansions[action.register.default] = ExpansionState.Expanded;
  }

  const updatedTarget = {
    expansions: { ...expansions },
    groups: groups ? { ...groups } : {},
  };

  updatedTarget.groups[action.register.key] = action.register.options;

  updatedContext[action.target] = updatedTarget;

  return updatedContext;
}

function doDeregister(context: FocusState, action: FocusAction): FocusState {
  if (!action.deregister) {
    return context;
  }

  const updatedContext: FocusState = { ...context };

  const groups = context[action.target].groups;
  const updatedTarget = {
    expansions: { ...context[action.target].expansions },
    groups: groups ? { ...groups } : {},
  };

  delete updatedTarget.groups[action.deregister];

  updatedContext[action.target] = updatedTarget;

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
