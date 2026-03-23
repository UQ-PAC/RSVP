import { useReducer } from "react";
import {
  reducer,
  ReportsContext,
  ReportsDispatchContext,
} from "../ReportsContext";

export function ReportsProvider({ children }) {
  const [context, dispatch] = useReducer(reducer, {
    selected: "",
    hovered: "",
  });

  return (
    <ReportsContext value={context}>
      <ReportsDispatchContext value={dispatch}>
        {children}
      </ReportsDispatchContext>
    </ReportsContext>
  );
}
