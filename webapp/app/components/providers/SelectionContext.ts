import { createContext, Dispatch, useContext } from "react";

interface SelectionState {
  selected?: string;
  hovered?: string;
  scroll?: "source" | "report" | "none";
}

export const emptySelection = {};

interface SelectionAction {
  type: "click" | "mouseEnter" | "mouseLeave" | "other";
  id?: string;
  scroll: "source" | "report" | "none";
}

export function reducer(
  context: SelectionState,
  action: SelectionAction,
): SelectionState {
  switch (action.type) {
    case "click":
      const selectionChanged = action.id !== context.selected;

      return {
        ...context,
        selected: !selectionChanged ? "" : action.id,
        scroll: action.scroll,
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
    case "other":
      return {
        ...context,
        selected: action.id ?? context.selected,
        scroll: action.scroll,
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
