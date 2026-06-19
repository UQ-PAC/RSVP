import { publish, useEventListener } from "./events";

var effect;

jest.mock("react", () => ({
  useEffect: jest.fn((func) => (effect = func)),
}));

test("subscribes and publishes events", () => {
  const listener = jest.fn();

  const addEventListener = jest.spyOn(document, "addEventListener");
  const removeEventListener = jest.spyOn(document, "removeEventListener");
  const dispatchEvent = jest.spyOn(document, "dispatchEvent");

  useEventListener("verificationError", listener);
  expect(effect).toBeDefined();

  // Call useEffect function to add subscription
  const cleanup = effect();
  expect(cleanup).toBeDefined();

  expect(addEventListener).toHaveBeenCalledTimes(1);
  expect(addEventListener).toHaveBeenCalledWith("verificationError", listener);

  // Trigger event
  publish("verificationError");

  expect(dispatchEvent).toHaveBeenCalledTimes(1);
  expect(listener).toHaveBeenCalledTimes(1);

  // Trigger irrelevant event
  publish("verificationComplete");
  expect(dispatchEvent).toHaveBeenCalledTimes(2);
  expect(listener).toHaveBeenCalledTimes(1);

  // Call useEffect cleanup function to remove subscription
  cleanup();
  expect(removeEventListener).toHaveBeenCalledTimes(1);
  expect(removeEventListener).toHaveBeenCalledWith(
    "verificationError",
    listener,
  );
});
