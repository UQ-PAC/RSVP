import { useReducer } from "react";
import {
  reducer,
  SelectionContext,
  SelectionDispatchContext,
} from "../SelectionContext";

export function SelectionProvider({ children }) {
  const [context, dispatch] = useReducer(reducer, {});

  return (
    <SelectionContext value={context}>
      <SelectionDispatchContext value={dispatch}>
        {children}
      </SelectionDispatchContext>
    </SelectionContext>
  );
}
