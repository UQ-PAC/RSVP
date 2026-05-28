import { createContext, Dispatch, useContext } from "react";

type ExpansionGroup = "drawer" | "report-group" | "source-file";

export enum ExpansionStatus {
  Expanded = 0,
  Collapsed = 1,
}

type ExpansionId = string;
type ExpansionMap = { [key: ExpansionId]: ExpansionStatus };
type ConflictMap = { [key: ExpansionId]: ExpansionId[] };

interface ExpansionState {
  [target: string]: {
    expansions: ExpansionMap;
    conflicts?: ConflictMap;
  };
}

export const emptyExpansionState: ExpansionState = {
  drawer: {
    expansions: {
      left: ExpansionStatus.Expanded,
      right: ExpansionStatus.Collapsed,
    },
  },
  "report-group": { expansions: {} },
  "source-file": { expansions: {}, conflicts: {} },
};

interface ExpansionAction {
  type: "toggle" | "register" | "deregister";
  group: ExpansionGroup;
  id: ExpansionId;
  status?: ExpansionStatus;
  conflict?: ExpansionId;
  kind?: ExpansionId;
}

export const ExpansionContext =
  createContext<ExpansionState>(emptyExpansionState);

export const ExpansionDispatchContext = createContext<
  Dispatch<ExpansionAction>
>(() => {});

/* istanbul ignore next */
export function useExpansion() {
  return useContext(ExpansionContext);
}

/* istanbul ignore next */
export function useExpansionDispatch() {
  return useContext(ExpansionDispatchContext);
}

export function expansionReducer(
  context: ExpansionState,
  action: ExpansionAction,
): ExpansionState {
  switch (action.type) {
    case "toggle":
      return doToggle(context, action);
    case "register":
      return doRegister(context, action);
    case "deregister":
      return doDeregister(context, action);
  }
}

function doToggle(
  context: ExpansionState,
  action: ExpansionAction,
): ExpansionState {
  if (action.status === undefined) {
    return context;
  }

  if (context[action.group].expansions[action.id] === action.status) {
    // Nothing to do
    return context;
  }

  const updatedContext: ExpansionState = { ...context };

  const expansions = context[action.group].expansions;
  const conflicts = context[action.group].conflicts;
  const updatedTarget = {
    expansions: { ...expansions },
    conflicts: conflicts ? { ...conflicts } : undefined,
  };

  const toFocus = getKey(action.kind, action.id);

  if (action.status === ExpansionStatus.Expanded) {
    // Collapse all conflicting expansion entries
    Object.values(updatedTarget.conflicts ?? {})
      .find((conflict) => conflict.some((item) => item === toFocus))
      ?.forEach((item) => {
        updatedTarget.expansions[item] = ExpansionStatus.Collapsed;
      });
  }

  // Set target expansion
  updatedTarget.expansions[toFocus] = action.status;

  updatedContext[action.group] = updatedTarget;

  return updatedContext;
}

function doRegister(
  context: ExpansionState,
  action: ExpansionAction,
): ExpansionState {
  if (!action.conflict || action.status === undefined) {
    return context;
  }

  const updatedContext: ExpansionState = { ...context };

  const expansions = context[action.group].expansions;
  const conflicts = context[action.group].conflicts;

  const updatedTarget = {
    expansions: { ...expansions },
    conflicts: conflicts ? { ...conflicts } : {},
  };

  const toAdd = getKey(action.kind, action.id);

  if (updatedTarget.conflicts[action.conflict]) {
    const group = updatedTarget.conflicts[action.conflict];

    updatedTarget.conflicts[action.conflict] = [...group, toAdd];

    // If expanding registered file, reset focus for all files in group
    if (action.status === ExpansionStatus.Expanded) {
      updatedTarget.expansions[action.conflict] = ExpansionStatus.Collapsed;
      group.forEach(
        (id) => (updatedTarget.expansions[id] = ExpansionStatus.Collapsed),
      );
    }
  } else {
    updatedTarget.conflicts[action.conflict] = [toAdd];
  }

  // Set focus for new file
  updatedTarget.expansions[toAdd] = action.status;

  updatedContext[action.group] = updatedTarget;

  return updatedContext;
}

function doDeregister(
  context: ExpansionState,
  action: ExpansionAction,
): ExpansionState {
  if (!context[action.group].conflicts) {
    // If no conflicts exist, nothing to do
    return context;
  }

  const updatedContext: ExpansionState = { ...context };

  const conflicts = context[action.group].conflicts as ConflictMap;
  const updatedTarget = {
    expansions: { ...context[action.group].expansions },
    conflicts: { ...conflicts },
  };

  const toRemove = getKey(action.kind, action.id);

  if (updatedTarget.conflicts[toRemove]) {
    // Deleting entire group, remove expansion state entries for all files in group
    updatedTarget.conflicts[toRemove].forEach((file) => {
      delete updatedTarget.expansions[file];
    });

    // Delete group
    delete updatedTarget.conflicts[toRemove];
  } else {
    // Deleted file must be in a group, find and delete it
    const [key, group] = Object.entries(conflicts).find(([, group]) =>
      group.some((item) => item === toRemove),
    ) ?? [undefined, undefined];

    if (key) {
      updatedTarget.conflicts[key] = group.filter((id) => id !== toRemove);

      // If deleted target was expanded, then need to select a new focus target,
      // most recently added file
      if (updatedTarget.expansions[toRemove] === ExpansionStatus.Expanded) {
        const newFocus = updatedTarget.conflicts[key].findLast(
          (id) => getKind(id) === action.kind,
        );

        if (newFocus) {
          updatedTarget.expansions[newFocus] = ExpansionStatus.Expanded;
        } else {
          // Can't find target of kind, just expand original
          updatedTarget.expansions[key] = ExpansionStatus.Expanded;
        }
      }
    }
  }

  // Remove expansion state entry for deleted file
  delete updatedTarget.expansions[toRemove];

  updatedContext[action.group] = updatedTarget;

  return updatedContext;
}

function getKind(id: ExpansionId): ExpansionId | undefined {
  return id[1] === "/" ? id[0] : undefined;
}

function getKey(kind: ExpansionId | undefined, id: ExpansionId): ExpansionId {
  return kind ? `${kind}/${id}` : id;
}
