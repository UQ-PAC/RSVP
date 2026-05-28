import { fireEvent, render, screen } from "@testing-library/react";

import { ExpansionStatus } from "../../lib/context/ExpansionContext";
import { Fallback } from "./Fallback";

var uploadsDrawer = ExpansionStatus.Expanded;
var expansionDispatch = jest.fn();

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
        left: uploadsDrawer,
      },
    },
  }),
  useExpansionDispatch: () => expansionDispatch,
}));

beforeEach(() => {
  expansionDispatch.mockClear();
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
  uploadsDrawer = ExpansionStatus.Expanded;
  const { rerender } = render(<Fallback instruction="Have a cry" />);

  const link = screen.getByTestId("source-files-upload-link");
  expect(link).toBeInTheDocument();

  fireEvent.click(link);
  expect(expansionDispatch).toHaveBeenCalledTimes(0);

  uploadsDrawer = ExpansionStatus.Collapsed;
  rerender(<Fallback instruction="Have a cry" />);

  fireEvent.click(link);
  expect(expansionDispatch).toHaveBeenCalledTimes(2);
  expect(expansionDispatch).toHaveBeenNthCalledWith(1, {
    type: "toggle",
    group: "drawer",
    id: "left",
    status: ExpansionStatus.Expanded,
  });
  expect(expansionDispatch).toHaveBeenNthCalledWith(2, {
    type: "toggle",
    group: "drawer",
    id: "right",
    status: ExpansionStatus.Collapsed,
  });
});
