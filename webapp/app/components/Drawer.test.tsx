/* eslint-disable no-var */
import { fireEvent, render, screen } from "@testing-library/react";

import { ExpansionState } from "../lib/context/FocusContext";
import { Drawer } from "./Drawer";

var right = ExpansionState.Expanded;
var left = ExpansionState.Expanded;
var focusDispatch = jest.fn();

jest.mock("@fortawesome/react-fontawesome", () => ({
  FontAwesomeIcon: jest.fn(({ icon }) => (
    <div data-testid="font-awesome-icon" data-icon={icon} />
  )),
}));

jest.mock("@fortawesome/free-solid-svg-icons", () => ({
  faCaretLeft: "faCaretLeft",
  faCaretRight: "faCaretRight",
}));

jest.mock("../lib/context/FocusContext", () => ({
  ...jest.requireActual("../lib/context/FocusContext"),
  useFocus: () => ({
    drawer: {
      expansions: {
        right,
        left,
      },
    },
  }),
  useFocusDispatch: () => focusDispatch,
}));

beforeEach(() => {
  focusDispatch.mockClear();
});

const children = [<div key="one" />, <div key="two" />];

describe("renders left", () => {
  test("expanded", () => {
    left = ExpansionState.Expanded;
    const { asFragment } = render(
      <Drawer title="Left" side="left">
        {children}
      </Drawer>,
    );
    expect(asFragment()).toMatchSnapshot();
  });

  test("collapsed", () => {
    left = ExpansionState.Collapsed;
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
    right = ExpansionState.Expanded;
    const { asFragment } = render(
      <Drawer title="Right" side="right">
        {children}
      </Drawer>,
    );
    expect(asFragment()).toMatchSnapshot();
  });

  test("collapsed", () => {
    right = ExpansionState.Collapsed;
    const { asFragment } = render(
      <Drawer title="Right" side="right">
        {children}
      </Drawer>,
    );
    expect(asFragment()).toMatchSnapshot();
  });
});

test("handles no children", () => {
  right = ExpansionState.Expanded;
  const { asFragment } = render(<Drawer title="Empty" side="right" />);
  expect(asFragment()).toMatchSnapshot();
});

test("triggers expansion", () => {
  left = ExpansionState.Collapsed;
  const { asFragment } = render(
    <Drawer title="Expand Me" side="left">
      {children}
    </Drawer>,
  );
  expect(asFragment()).toMatchSnapshot();

  const tab = screen.getByTestId("drawer-tab");
  expect(tab).toBeInTheDocument();

  fireEvent.click(tab);
  expect(focusDispatch).toHaveBeenCalledTimes(1);
  expect(focusDispatch).toHaveBeenCalledWith({
    type: "focus",
    target: "drawer",
    focus: {
      key: "left",
      value: ExpansionState.Expanded,
    },
  });
});

test("triggers collapse", () => {
  left = ExpansionState.Expanded;
  const { asFragment } = render(
    <Drawer title="Collapse Me" side="left">
      {children}
    </Drawer>,
  );
  expect(asFragment()).toMatchSnapshot();

  const tab = screen.getByTestId("drawer-tab");
  expect(tab).toBeInTheDocument();

  fireEvent.click(tab);
  expect(focusDispatch).toHaveBeenCalledTimes(1);
  expect(focusDispatch).toHaveBeenCalledWith({
    type: "focus",
    target: "drawer",
    focus: {
      key: "left",
      value: ExpansionState.Collapsed,
    },
  });
});
