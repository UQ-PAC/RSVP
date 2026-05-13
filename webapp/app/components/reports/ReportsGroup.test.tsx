/* eslint-disable @typescript-eslint/no-explicit-any */
/* eslint-disable no-var */
import { fireEvent, render, screen } from "@testing-library/react";

import { ExpansionState } from "../../lib/context/FocusContext";
import { ReportsGroup } from "./ReportsGroup";

var testFileOne = ExpansionState.Expanded;
var testFileTwo = ExpansionState.Expanded;

var focusDispatch = jest.fn();
var selectionDispatch = jest.fn();

jest.mock("@fortawesome/react-fontawesome", () => ({
  FontAwesomeIcon: jest.fn(({ icon }) => (
    <div data-testid="font-awesome-icon" data-icon={icon} />
  )),
}));

jest.mock("@fortawesome/free-regular-svg-icons", () => ({
  faSquareMinus: "faSquareMinus",
  faSquarePlus: "faSquarePlus",
}));

jest.mock("@fortawesome/free-solid-svg-icons", () => ({
  faSquareMinus: "faSquareMinus",
  faSquarePlus: "faSquarePlus",
}));

jest.mock("../../lib/context/FocusContext", () => ({
  ...jest.requireActual("../../lib/context/FocusContext"),
  useFocus: () => ({
    "report-group": {
      expansions: {
        "section-test-file-one.txt": testFileOne,
        "section-test-file-two.txt": testFileTwo,
      },
    },
  }),
  useFocusDispatch: () => focusDispatch,
}));

jest.mock("../../lib/context/SelectionContext", () => ({
  useSelectionDispatch: () => selectionDispatch,
}));

jest.mock("./ReportItem", () => ({
  ReportItem: jest.fn(({ report }) => (
    <div data-testid="report-item" data-reportid={report.id} />
  )),
}));

beforeEach(() => {
  focusDispatch.mockClear();
  selectionDispatch.mockClear();
});

test("renders", () => {
  testFileOne = ExpansionState.Expanded;

  const file = "test-file-one.txt";
  const reports = [{ id: "one" }, { id: "two" }] as any[];

  const { asFragment, rerender } = render(
    <ReportsGroup
      section="section"
      id={file}
      name={file + " (2)"}
      reports={reports}
    />,
  );
  expect(asFragment()).toMatchSnapshot();

  testFileOne = ExpansionState.Collapsed;

  rerender(
    <ReportsGroup section="section" id={file} name={file} reports={reports} />,
  );
  expect(asFragment()).toMatchSnapshot();
});

test("triggers expansion", () => {
  testFileOne = ExpansionState.Collapsed;
  testFileTwo = ExpansionState.Expanded;

  const fileOne = "test-file-one.txt";
  const fileTwo = "test-file-two.txt";

  const focusAction = {
    type: "focus",
    target: "report-group",
  };

  const { asFragment } = render(
    <>
      <ReportsGroup
        section="section"
        id={fileOne}
        name={fileOne}
        reports={[{ id: "one" }, { id: "two" }] as any[]}
      />
      <ReportsGroup
        section="section"
        id={fileTwo}
        name={fileTwo}
        reports={[{ id: "three" }, { id: "four" }] as any[]}
      />
    </>,
  );
  expect(asFragment()).toMatchSnapshot();

  // Trigger expansion of group one
  const headerOne = screen.getByTestId(`reports-group-${fileOne}-header`);
  expect(headerOne).toBeInTheDocument();

  const toggleOne = headerOne.querySelector(".reports-group-toggle");
  expect(toggleOne).toBeTruthy();
  expect(toggleOne).toBeInTheDocument();

  fireEvent.click(toggleOne!);
  expect(selectionDispatch).toHaveBeenCalledTimes(1);
  expect(selectionDispatch).toHaveBeenCalledWith({
    scroll: "none",
  });
  expect(focusDispatch).toHaveBeenCalledTimes(1);
  expect(focusDispatch).toHaveBeenCalledWith({
    ...focusAction,
    focus: { key: `section-${fileOne}`, value: ExpansionState.Expanded },
  });

  // Trigger collapse of group two
  const headerTwo = screen.getByTestId(`reports-group-${fileTwo}-header`);
  expect(headerTwo).toBeInTheDocument();
  const toggleTwo = headerTwo.querySelector(".reports-group-toggle");
  expect(toggleTwo).toBeTruthy();
  expect(toggleTwo).toBeInTheDocument();

  fireEvent.click(toggleTwo!);
  expect(selectionDispatch).toHaveBeenCalledTimes(2);
  expect(focusDispatch).toHaveBeenCalledTimes(2);
  expect(focusDispatch).toHaveBeenLastCalledWith({
    ...focusAction,
    focus: { key: `section-${fileTwo}`, value: ExpansionState.Collapsed },
  });
});
