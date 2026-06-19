import { act, render } from "@testing-library/react";
import { AnalysisGroupProvider } from "./AnalysisGroupProvider";

var analysisGroupReducer = jest.fn((context, action) => action.update);
var verificationDispatch = jest.fn();
var analysisGroupDispatch = undefined;
var emptyAnalysisGroup = {
  files: [],
  diffs: {},
  verifyPending: false,
  verifyRequested: false,
};
var baseVerificationContext = {
  one: {
    name: "one",
  },
  two: {
    name: "two",
  },
} as any;
var verificationContext = { ...baseVerificationContext };

jest.mock("../../lib/context/AnalysisGroupContext", () => ({
  AnalysisGroupContext: jest.fn(({ value, children }) => (
    <div
      data-testid={`${value.name}-context`}
      data-diffs={JSON.stringify(value.diffs)}
    >
      {children}
    </div>
  )),
  AnalysisGroupDispatchContext: jest.fn(({ value, children }) => {
    analysisGroupDispatch = value;
    return <div data-testid="dispatch-context">{children}</div>;
  }),
  emptyAnalysisGroup,
  reducer: (context, action) =>
    analysisGroupReducer.call(undefined, context, action),
}));

jest.mock("../../lib/context/VerificationContext", () => ({
  useVerification: () => verificationContext,
  useVerificationDispatch: () => verificationDispatch,
}));

beforeEach(() => {
  analysisGroupReducer.mockClear();
  verificationDispatch.mockClear();
  verificationContext = { ...baseVerificationContext };
});

test("renders", () => {
  const { asFragment } = render(
    <AnalysisGroupProvider group={verificationContext.one}>
      <div data-testid="child">{"I'm a child"}</div>
    </AnalysisGroupProvider>,
  );
  expect(asFragment()).toMatchSnapshot();
});

test("updates local context", () => {
  const childOne = <div data-testid="child-one">{"I'm a child"}</div>;
  const childTwo = <div data-testid="child-two">{"I'm an adult"}</div>;

  const { asFragment, rerender } = render(
    <AnalysisGroupProvider group={verificationContext.one}>
      {childOne}
    </AnalysisGroupProvider>,
  );
  expect(asFragment()).toMatchSnapshot();
  expect(verificationDispatch).not.toHaveBeenCalled();
  expect(analysisGroupReducer).not.toHaveBeenCalled();

  // Update local context when group changes
  rerender(
    <AnalysisGroupProvider group={verificationContext.two}>
      {childOne}
    </AnalysisGroupProvider>,
  );
  expect(asFragment()).toMatchSnapshot();
  expect(verificationDispatch).not.toHaveBeenCalled();
  expect(analysisGroupReducer).toHaveBeenCalledTimes(1);
  expect(analysisGroupReducer).toHaveBeenCalledWith(verificationContext.one, {
    type: "update",
    update: verificationContext.two,
  });

  // Don't update local context when only children change
  rerender(
    <AnalysisGroupProvider group={verificationContext.two}>
      {childTwo}
    </AnalysisGroupProvider>,
  );
  expect(asFragment()).toMatchSnapshot();
  expect(verificationDispatch).not.toHaveBeenCalled();
  expect(analysisGroupReducer).toHaveBeenCalledTimes(1);

  // Update local context when global context changes
  act(() => {
    verificationContext.two = {
      ...verificationContext.two,
      diffs: ["I'm a diff!"],
    };
  });

  rerender(
    <AnalysisGroupProvider group={verificationContext.two}>
      {childTwo}
    </AnalysisGroupProvider>,
  );

  expect(asFragment()).toMatchSnapshot();
  expect(verificationDispatch).not.toHaveBeenCalled();
  expect(analysisGroupReducer).toHaveBeenCalledTimes(2);
  expect(analysisGroupReducer).toHaveBeenNthCalledWith(
    2,
    baseVerificationContext.two,
    {
      type: "update",
      update: verificationContext.two,
    },
  );
});

test("updates global context", () => {
  const child = <div data-testid="child">{"I'm a child"}</div>;

  const { rerender } = render(
    <AnalysisGroupProvider group={verificationContext.one}>
      {child}
    </AnalysisGroupProvider>,
  );

  expect(verificationDispatch).not.toHaveBeenCalled();
  expect(analysisGroupReducer).not.toHaveBeenCalled();
  expect(analysisGroupDispatch).toBeDefined();

  act(() => {
    (analysisGroupDispatch as any)({
      type: "update",
      update: { name: "one", impacts: ["I'm an impact"] },
    });
  });

  expect(analysisGroupReducer).toHaveBeenCalledTimes(1);

  rerender(
    <AnalysisGroupProvider group={verificationContext.one}>
      {child}
    </AnalysisGroupProvider>,
  );

  expect(verificationDispatch).toHaveBeenCalledTimes(1);
  expect(analysisGroupReducer).toHaveBeenCalledTimes(1);
});
