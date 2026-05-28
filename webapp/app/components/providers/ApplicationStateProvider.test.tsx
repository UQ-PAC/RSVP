import { act, render } from "@testing-library/react";
import { emptyVerification } from "../../lib/context/VerificationContext";
import { ApplicationStateProvider } from "./ApplicationStateProvider";

var verificationReducer = jest.fn();
var verifyComplete;

jest.mock("../../lib/context/ExpansionContext", () => ({
  ExpansionContext: jest.fn(({ children }) => (
    <div data-testid="expansion-context">{children}</div>
  )),
  ExpansionDispatchContext: jest.fn(({ children }) => (
    <div data-testid="expansion-dispatch-context">{children}</div>
  )),
  emptyFocus: {},
  expansionReducer: () => jest.fn().call(undefined),
}));

jest.mock("../../lib/context/SelectionContext", () => ({
  SelectionContext: jest.fn(({ children }) => (
    <div data-testid="selection-context">{children}</div>
  )),
  SelectionDispatchContext: jest.fn(({ children }) => (
    <div data-testid="selection-dispatch-context">{children}</div>
  )),
  emptySelection: {},
  selectionReducer: () => jest.fn().call(undefined),
}));

jest.mock("../../lib/context/VerificationContext", () => ({
  VerificationContext: jest.fn(({ children }) => (
    <div data-testid="verification-context">{children}</div>
  )),
  VerificationDispatchContext: jest.fn(({ children }) => (
    <div data-testid="verification-dispatch-context">{children}</div>
  )),
  emptyVerification: {},
  verificationReducer: (context, action) =>
    verificationReducer.call(undefined, context, action),
}));

jest.mock("../../lib/events", () => ({
  useEventListener: jest.fn((event, listener) => {
    if (event === "verificationComplete") {
      verifyComplete = listener;
    }
    return jest.fn();
  }),
}));

test("renders", () => {
  const { asFragment } = render(
    <ApplicationStateProvider>
      <div data-testid="child">{"I'm a child"}</div>
    </ApplicationStateProvider>,
  );
  expect(asFragment()).toMatchSnapshot();
});

beforeEach(() => {
  verificationReducer.mockClear();
});

test("handles verification complete events", () => {
  const { rerender } = render(
    <ApplicationStateProvider>
      <div data-testid="child">{"I'm a child"}</div>
    </ApplicationStateProvider>,
  );

  expect(verificationReducer).not.toHaveBeenCalled();

  // Simulate verification completion event
  expect(verifyComplete).toBeDefined();
  act(() => {
    verifyComplete();
    rerender(
      <ApplicationStateProvider>
        <div data-testid="child">{"I'm a child"}</div>
      </ApplicationStateProvider>,
    );
  });

  expect(verificationReducer).toHaveBeenCalled();
  expect(verificationReducer).toHaveBeenCalledWith(emptyVerification, {
    type: "complete",
  });
});
