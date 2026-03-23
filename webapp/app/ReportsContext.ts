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

interface ReportsState {
  selected?: string;
  hovered?: string;
}

interface Action {
  type: "click" | "mouseEnter" | "mouseLeave" | "reports";
  id?: string;
}

export function reducer(context: ReportsState, action: Action): ReportsState {
  switch (action.type) {
    case "click":
      return {
        ...context,
        selected: action.id === context.selected ? "" : action.id,
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

export const ReportsContext = createContext<ReportsState>({
  selected: "",
  hovered: "",
});
export const ReportsDispatchContext = createContext<Dispatch<Action>>(
  (_: Action) => {},
);

export function useReports() {
  return useContext(ReportsContext);
}

export function useReportsDispatch() {
  return useContext(ReportsDispatchContext);
}
