/* eslint-disable no-var */
import { fireEvent, render, screen } from "@testing-library/react";

import { ExpansionState } from "../../lib/context/FocusContext";
import { Fallback } from "./Fallback";

var uploadsDrawer = ExpansionState.Expanded;
var focusDispatch = jest.fn();

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
        left: uploadsDrawer,
      },
    },
  }),
  useFocusDispatch: () => focusDispatch,
}));

beforeEach(() => {
  focusDispatch.mockClear();
});

test("renders", () => {
  const { asFragment, rerender } = render(
    <Fallback instruction="Clap your hands" />,
  );
  expect(asFragment()).toMatchSnapshot();

  rerender(<Fallback instruction="Do a dance" target="your face" />);
  expect(asFragment()).toMatchSnapshot();
});

test("expands uploads drawer", () => {
  uploadsDrawer = ExpansionState.Expanded;
  const { rerender } = render(<Fallback instruction="Have a cry" />);

  const link = screen.getByTestId("source-files-upload-link");
  expect(link).toBeInTheDocument();

  fireEvent.click(link);
  expect(focusDispatch).toHaveBeenCalledTimes(0);

  uploadsDrawer = ExpansionState.Collapsed;
  rerender(<Fallback instruction="Have a cry" />);

  fireEvent.click(link);
  expect(focusDispatch).toHaveBeenCalledTimes(2);
  expect(focusDispatch).toHaveBeenNthCalledWith(1, {
    type: "focus",
    target: "drawer",
    focus: { key: "left", value: ExpansionState.Expanded },
  });
  expect(focusDispatch).toHaveBeenNthCalledWith(2, {
    type: "focus",
    target: "drawer",
    focus: { key: "right", value: ExpansionState.Collapsed },
  });
});
