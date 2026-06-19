import { act, render } from "@testing-library/react";
import { SourceFileViewer } from "./SourceFileViewer";

var emptyContext = {};
var withGroups = {
  "group one": { name: "group one" },
  "group two": { name: "group two" },
};

var verificationContext;

jest.mock("../../lib/context/VerificationContext", () => ({
  useVerification: () => verificationContext,
}));

jest.mock("./Fallback", () => ({
  Fallback: jest.fn(({ instruction }) => (
    <div data-testid="fallback" data-instruction={instruction} />
  )),
}));

jest.mock("../providers/AnalysisGroupProvider", () => ({
  AnalysisGroupProvider: jest.fn(({ group, children }) => (
    <div data-testid="analysis-group-provider" data-group={group.name}>
      {children}
    </div>
  )),
}));

jest.mock("./AnalysisGroup", () => ({
  AnalysisGroup: jest.fn(() => <div data-testid="analysis-group" />),
}));

beforeEach(() => {
  verificationContext = emptyContext;
});

test("renders", () => {
  const { asFragment, rerender } = render(<SourceFileViewer />);
  expect(asFragment()).toMatchSnapshot();

  act(() => {
    verificationContext = withGroups;
    rerender(<SourceFileViewer />);
  });

  expect(asFragment()).toMatchSnapshot();
});
