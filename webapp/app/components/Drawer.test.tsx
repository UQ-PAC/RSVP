import { fireEvent, render, screen } from "@testing-library/react";

import { ExpansionStatus } from "../lib/context/ExpansionContext";
import { Drawer } from "./Drawer";

var right = ExpansionStatus.Expanded;
var left = ExpansionStatus.Expanded;
var expansionDispatch = jest.fn();

jest.mock("@fortawesome/react-fontawesome", () => ({
  FontAwesomeIcon: jest.fn(({ icon }) => (
    <div data-testid="font-awesome-icon" data-icon={icon} />
  )),
}));

jest.mock("@fortawesome/free-solid-svg-icons", () => ({
  faCaretLeft: "faCaretLeft",
  faCaretRight: "faCaretRight",
}));

jest.mock("../lib/context/ExpansionContext", () => ({
  ...jest.requireActual("../lib/context/ExpansionContext"),
  useExpansion: () => ({
    drawer: {
      expansions: {
        right,
        left,
      },
    },
  }),
  useExpansionDispatch: () => expansionDispatch,
}));

beforeEach(() => {
  expansionDispatch.mockClear();
});

const children = [<div key="one" />, <div key="two" />];

describe("renders left", () => {
  test("expanded", () => {
    left = ExpansionStatus.Expanded;
    const { asFragment } = render(
      <Drawer title="Left" side="left">
        {children}
      </Drawer>,
    );
    expect(asFragment()).toMatchSnapshot();
  });

  test("collapsed", () => {
    left = ExpansionStatus.Collapsed;
    const { asFragment } = render(
      <Drawer title="Left" side="left">
        {children}
      </Drawer>,
    );
    expect(asFragment()).toMatchSnapshot();
  });
});

describe("renders right", () => {
  test("expanded", () => {
    right = ExpansionStatus.Expanded;
    const { asFragment } = render(
      <Drawer title="Right" side="right">
        {children}
      </Drawer>,
    );
    expect(asFragment()).toMatchSnapshot();
  });

  test("collapsed", () => {
    right = ExpansionStatus.Collapsed;
    const { asFragment } = render(
      <Drawer title="Right" side="right">
        {children}
      </Drawer>,
    );
    expect(asFragment()).toMatchSnapshot();
  });
});

test("handles no children", () => {
  right = ExpansionStatus.Expanded;
  const { asFragment } = render(<Drawer title="Empty" side="right" />);
  expect(asFragment()).toMatchSnapshot();
});

test("triggers expansion", () => {
  left = ExpansionStatus.Collapsed;
  const { asFragment } = render(
    <Drawer title="Expand Me" side="left">
      {children}
    </Drawer>,
  );
  expect(asFragment()).toMatchSnapshot();

  const tab = screen.getByTestId("drawer-tab");
  expect(tab).toBeInTheDocument();

  fireEvent.click(tab);
  expect(expansionDispatch).toHaveBeenCalledTimes(1);
  expect(expansionDispatch).toHaveBeenCalledWith({
    type: "toggle",
    group: "drawer",
    id: "left",
    status: ExpansionStatus.Expanded,
  });
});

test("triggers collapse", () => {
  left = ExpansionStatus.Expanded;
  const { asFragment } = render(
    <Drawer title="Collapse Me" side="left">
      {children}
    </Drawer>,
  );
  expect(asFragment()).toMatchSnapshot();

  const tab = screen.getByTestId("drawer-tab");
  expect(tab).toBeInTheDocument();

  fireEvent.click(tab);
  expect(expansionDispatch).toHaveBeenCalledTimes(1);
  expect(expansionDispatch).toHaveBeenCalledWith({
    type: "toggle",
    group: "drawer",
    id: "left",
    status: ExpansionStatus.Collapsed,
  });
});
