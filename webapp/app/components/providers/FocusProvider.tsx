import {
  emptyFocus,
  FocusContext,
  FocusDispatchContext,
  reducer,
} from "./FocusContext";
import { useReducer } from "react";

export function FocusProvider({ children }) {
  const [context, dispatch] = useReducer(reducer, emptyFocus);

  return (
    <FocusContext value={context}>
      <FocusDispatchContext value={dispatch}>{children}</FocusDispatchContext>
    </FocusContext>
  );
}
