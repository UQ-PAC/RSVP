import { useReducer } from "react";
import {
  emptySelection,
  reducer,
  SelectionContext,
  SelectionDispatchContext,
} from "./SelectionContext";

export function SelectionProvider({ children }) {
  const [context, dispatch] = useReducer(reducer, emptySelection);

  return (
    <SelectionContext value={context}>
      <SelectionDispatchContext value={dispatch}>
        {children}
      </SelectionDispatchContext>
    </SelectionContext>
  );
}
