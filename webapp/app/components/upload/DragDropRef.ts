import { VerificationFile } from "@/app/types";
import { useCallback } from "react";

// Hack for drag and drop with React 19 typings
// Source: https://github.com/react-dnd/react-dnd/issues/3655

/**
 * Returns a callback ref that calls `drag(element)` when the DOM node is attached.
 */
export function useDragRef(drag: (el: HTMLSpanElement) => void) {
  return useCallback(
    (element: HTMLSpanElement | null) => {
      if (element) {
        drag(element);
      }
    },
    [drag],
  );
}

export function useDropRef(drop: (el: HTMLSpanElement) => void) {
  return useCallback(
    (element: HTMLSpanElement | null) => {
      if (element) {
        drop(element);
      }
    },
    [drop],
  );
}
