import { createContext, Dispatch, useContext } from "react";

interface SelectionState {
  selected?: string;
  hovered?: string;
  loc?: string;
  scroll?: "source" | "report" | "none";
}

export const emptySelection = {};

interface SelectionAction {
  type: "click" | "mouseEnter" | "mouseLeave" | "other";
  id?: string;
  loc?: string;
  scroll: "source" | "report" | "none";
}

export function selectionReducer(
  context: SelectionState,
  action: SelectionAction,
): SelectionState {
  switch (action.type) {
    case "click":
      if (action.id) {
        const selectionChanged = action.id !== context.selected;
        return {
          ...context,
          selected: !selectionChanged ? "" : action.id,
          scroll: action.scroll,
        };
      } else if (action.loc) {
        return {
          ...context,
          scroll: action.scroll,
          loc: action.loc,
        };
      }
      break;
    case "mouseEnter":
      return {
        ...context,
        hovered: action.id ?? context.hovered,
        loc: action.loc,
        scroll: action.scroll,
      };
    case "mouseLeave":
      if (action.id && action.id === context.hovered) {
        return {
          ...context,
          hovered: "",
          loc: undefined,
          scroll: action.scroll,
        };
      } else {
        return {
          ...context,
          loc: undefined,
          scroll: action.scroll,
        };
      }
    case "other":
      return {
        ...context,
        scroll: action.scroll,
        loc: undefined,
      };
  }
  return context;
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
