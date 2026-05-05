/* eslint-disable no-var */
import { fireEvent, render, screen } from "@testing-library/react";

import { ExpansionState } from "../../lib/context/FocusContext";
import { VerifyButton } from "./VerifyButton";

var reportsDrawer = ExpansionState.Expanded;
var focusDispatch = jest.fn();
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

jest.mock("../../lib/context/FocusContext", () => ({
  ...jest.requireActual("../../lib/context/FocusContext"),
  useFocus: () => ({
    drawer: {
      expansions: {
        right: reportsDrawer,
      },
    },
  }),
  useFocusDispatch: () => focusDispatch,
}));

jest.mock("../../lib/context/VerificationContext", () => ({
  useVerification: jest.fn(() => ({})),
  useVerificationDispatch: () => verificationDispatch,
}));

beforeEach(() => {
  focusDispatch.mockClear();
  verificationDispatch.mockClear();
});

test("renders", () => {
  const { asFragment } = render(<VerifyButton />);
  expect(asFragment()).toMatchSnapshot();
});

test("triggers verification", () => {
  reportsDrawer = ExpansionState.Expanded;
  render(<VerifyButton />);
  const button = screen.getByTestId("verify-button");
  expect(button).toBeInTheDocument();

  fireEvent.click(button);
  expect(verificationDispatch).toHaveBeenCalledTimes(2);
  expect(verificationDispatch).toHaveBeenNthCalledWith(1, {
    type: "pre-verify",
  });
  expect(verificationDispatch).toHaveBeenNthCalledWith(2, { type: "verify" });
  expect(focusDispatch).toHaveBeenCalledTimes(0);
});

test("expands reports drawer", () => {
  reportsDrawer = ExpansionState.Collapsed;
  render(<VerifyButton />);

  const button = screen.getByTestId("verify-button");
  expect(button).toBeInTheDocument();

  fireEvent.click(button);
  expect(verificationDispatch).toHaveBeenCalledTimes(2);
  expect(focusDispatch).toHaveBeenCalledTimes(2);
  expect(focusDispatch).toHaveBeenNthCalledWith(1, {
    type: "focus",
    target: "drawer",
    focus: { key: "left", value: ExpansionState.Collapsed },
  });
  expect(focusDispatch).toHaveBeenNthCalledWith(2, {
    type: "focus",
    target: "drawer",
    focus: { key: "right", value: ExpansionState.Expanded },
  });
});
