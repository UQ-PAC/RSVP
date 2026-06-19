import { fireEvent, render, screen } from "@testing-library/react";

import { ExpansionStatus } from "../../lib/context/ExpansionContext";
import { VerifyButton } from "./VerifyButton";

var uploadDrawer = ExpansionStatus.Expanded;
var reportsDrawer = ExpansionStatus.Expanded;
var analysisGroupValidationResult = { error: false };
var expansionDispatch = jest.fn();
var verificationDispatch = jest.fn();

jest.mock("next/font/google", () => ({
  Lexend_Deca: jest.fn().mockReturnValue({ className: "lexend-deca" }),
}));

jest.mock("@fortawesome/react-fontawesome", () => ({
  FontAwesomeIcon: jest.fn(({ icon }) => (
    <div data-testid="font-awesome-icon" data-icon={icon} />
  )),
}));

jest.mock("@fortawesome/free-solid-svg-icons", () => ({
  faFileShield: "faFileShield",
}));

jest.mock("../../lib/context/ExpansionContext", () => ({
  ...jest.requireActual("../../lib/context/ExpansionContext"),
  useExpansion: () => ({
    drawer: {
      expansions: {
        left: uploadDrawer,
        right: reportsDrawer,
      },
    },
  }),
  useExpansionDispatch: () => expansionDispatch,
}));

jest.mock("../../lib/context/VerificationContext", () => ({
  useVerification: jest.fn(() => ({ group: {} })),
  useVerificationDispatch: () => verificationDispatch,
}));

jest.mock("../../lib/util", () => ({
  checkAnalysisGroup: jest.fn(() => analysisGroupValidationResult),
}));

beforeEach(() => {
  expansionDispatch.mockClear();
  verificationDispatch.mockClear();
});

test("renders", () => {
  const { asFragment } = render(<VerifyButton />);
  expect(asFragment()).toMatchSnapshot();
});

test("triggers verification", () => {
  analysisGroupValidationResult = { error: false };

  render(<VerifyButton />);
  const button = screen.getByTestId("verify-button");
  expect(button).toBeInTheDocument();

  fireEvent.click(button);
  expect(verificationDispatch).toHaveBeenCalledTimes(1);
  expect(verificationDispatch).toHaveBeenCalledWith({ type: "verify" });
});

test("doesn't trigger verification on error", () => {
  analysisGroupValidationResult = { error: true };

  render(<VerifyButton />);
  const button = screen.getByTestId("verify-button");
  expect(button).toBeInTheDocument();

  fireEvent.click(button);
  expect(verificationDispatch).toHaveBeenCalledTimes(0);
});

test("expands reports drawer on valid request", () => {
  analysisGroupValidationResult = { error: false };

  // Don't trigger expansion if already expanded
  reportsDrawer = ExpansionStatus.Expanded;
  const { rerender } = render(<VerifyButton />);

  const button = screen.getByTestId("verify-button");
  expect(button).toBeInTheDocument();
  fireEvent.click(button);

  expect(expansionDispatch).toHaveBeenCalledTimes(0);

  // Trigger expansion if reports drawer is collapsed
  reportsDrawer = ExpansionStatus.Collapsed;
  rerender(<VerifyButton />);

  fireEvent.click(button);
  expect(expansionDispatch).toHaveBeenCalledTimes(2);
  expect(expansionDispatch).toHaveBeenNthCalledWith(1, {
    type: "toggle",
    group: "drawer",
    id: "left",
    status: ExpansionStatus.Collapsed,
  });
  expect(expansionDispatch).toHaveBeenNthCalledWith(2, {
    type: "toggle",
    group: "drawer",
    id: "right",
    status: ExpansionStatus.Expanded,
  });
});

test("expands upload drawer on error", () => {
  analysisGroupValidationResult = { error: true };

  // Don't trigger expansion if already expanded
  uploadDrawer = ExpansionStatus.Expanded;
  const { rerender } = render(<VerifyButton />);

  const button = screen.getByTestId("verify-button");
  expect(button).toBeInTheDocument();
  fireEvent.click(button);

  expect(expansionDispatch).toHaveBeenCalledTimes(0);

  // Trigger expansion if upload drawer is collapsed
  uploadDrawer = ExpansionStatus.Collapsed;
  rerender(<VerifyButton />);

  fireEvent.click(button);
  expect(expansionDispatch).toHaveBeenCalledTimes(2);
  expect(expansionDispatch).toHaveBeenNthCalledWith(1, {
    type: "toggle",
    group: "drawer",
    id: "right",
    status: ExpansionStatus.Collapsed,
  });
  expect(expansionDispatch).toHaveBeenNthCalledWith(2, {
    type: "toggle",
    group: "drawer",
    id: "left",
    status: ExpansionStatus.Expanded,
  });
});
