import { useEffect } from "react";

export type GlobalEvent =
  | "verificationRequested"
  | "verificationPending"
  | "verificationComplete"
  | "verificationError";

function subscribe(eventName: GlobalEvent, listener: EventListener): void {
  /* istanbul ignore else - window is not defined when running on server */
  if (typeof window !== "undefined") {
    document.addEventListener(eventName, listener);
  }
}

function unsubscribe(eventName: GlobalEvent, listener: EventListener): void {
  /* istanbul ignore else - window is not defined when running on server */
  if (typeof window !== "undefined") {
    document.removeEventListener(eventName, listener);
  }
}

export function publish(eventName: GlobalEvent, data?: object): void {
  /* istanbul ignore else - window is not defined when running on server */
  if (typeof window !== "undefined") {
    const event = new CustomEvent(eventName, { detail: data });
    document.dispatchEvent(event);
  }
}

export function useEventListener(
  eventName: GlobalEvent,
  handler: EventListener,
) {
  useEffect(() => {
    subscribe(eventName, handler);
    return () => {
      unsubscribe(eventName, handler);
    };
  }, [eventName, handler]);
}
