import { act, render } from "@testing-library/react";
import { FileUploader } from "./FileUploader";

var verificationDispatch = jest.fn();
var basicVerification = {
  "group one": { name: "group one" },
  "group two": { name: "group two" },
  "group three": { name: "group three" },
} as any;
var verification = basicVerification;

// Store callbacks for invocation as child components are mocked
var openCreatePolicySetForm;
var cancelCreatePolicySet;
var createPolicySet;
var removeGroupCallback;

jest.mock("./AnalysisGroup", () => ({
  AnalysisGroup: jest.fn(({ removeGroup }) => {
    removeGroupCallback = removeGroup;
    return <div data-testid="analysis-group" />;
  }),
}));

jest.mock("./CreateContextButton", () => ({
  CreateContextButton: jest.fn(({ onclick }) => {
    openCreatePolicySetForm = onclick;
    return <div data-testid="create-context-button" />;
  }),
}));

jest.mock("../../lib/context/VerificationContext", () => ({
  useVerification: () => verification,
  useVerificationDispatch: () => verificationDispatch,
}));

jest.mock("../providers/AnalysisGroupProvider", () => ({
  AnalysisGroupProvider: jest.fn(({ group, children }) => {
    return (
      <div data-testid="analysis-group-provider" data-group={group.name}>
        {children}
      </div>
    );
  }),
}));

jest.mock("./NewGroupForm", () => ({
  NewGroupForm: jest.fn(({ index, create, cancel, existing }) => {
    createPolicySet = create;
    cancelCreatePolicySet = cancel;
    return (
      <div
        data-testid="new-group-form"
        data-index={index}
        data-existing={existing.join(" ")}
      />
    );
  }),
}));

beforeEach(() => {
  verificationDispatch.mockClear();
  verification = basicVerification;
});

test("renders", () => {
  const { asFragment, rerender } = render(<FileUploader />);
  expect(asFragment()).toMatchSnapshot();

  verification = {};
  rerender(<FileUploader />);
  expect(asFragment()).toMatchSnapshot();
});

test("opens create policy form", () => {
  const { asFragment, rerender } = render(<FileUploader />);
  expect(asFragment()).toMatchSnapshot();

  // Trigger form open
  expect(openCreatePolicySetForm).toBeDefined();
  act(() => {
    openCreatePolicySetForm();
  });

  rerender(<FileUploader />);
  expect(asFragment()).toMatchSnapshot();

  // Trigger form cancel
  expect(cancelCreatePolicySet).toBeDefined();
  act(() => {
    cancelCreatePolicySet();
  });

  rerender(<FileUploader />);
  expect(asFragment()).toMatchSnapshot();
});

test("creates policy set", () => {
  const { asFragment, rerender } = render(<FileUploader />);

  // Trigger form open to check creation closes form
  expect(openCreatePolicySetForm).toBeDefined();
  act(() => {
    openCreatePolicySetForm();
  });

  expect(asFragment()).toMatchSnapshot();

  // Trigger policy set creation
  expect(createPolicySet).toBeDefined();
  act(() => {
    createPolicySet("policy set three");
  });

  expect(verificationDispatch).toHaveBeenCalledTimes(1);
  expect(verificationDispatch).toHaveBeenCalledWith({
    type: "add",
    group: "policy set three",
  });

  // Check form closed after creation
  rerender(<FileUploader />);
  expect(asFragment()).toMatchSnapshot();
});

test("deletes policy set", () => {
  verification = {
    "policy set": {},
  };

  render(<FileUploader />);

  // Trigger policy set creation
  expect(removeGroupCallback).toBeDefined();
  removeGroupCallback();

  expect(verificationDispatch).toHaveBeenCalledTimes(1);
  expect(verificationDispatch).toHaveBeenCalledWith({
    type: "remove",
    group: "policy set",
  });
});
