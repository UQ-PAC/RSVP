import { fireEvent, render, screen } from "@testing-library/react";

import { faFileLines } from "@fortawesome/free-solid-svg-icons";
import { ExpansionStatus } from "../../lib/context/ExpansionContext";
import { ReportGroup } from "./ReportGroup";

var testFileOne = ExpansionStatus.Expanded;
var testFileTwo = ExpansionStatus.Expanded;

var expansionDispatch = jest.fn();
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
  faSquareMinus: "faSquareMinus-solid",
  faSquarePlus: "faSquarePlus-solid",
  faFileLines: "faFileLines",
}));

jest.mock("../../lib/context/ExpansionContext", () => ({
  ...jest.requireActual("../../lib/context/ExpansionContext"),
  useExpansion: () => ({
    "report-group": {
      expansions: {
        "section-test-file-one.txt": testFileOne,
        "section-test-file-two.txt": testFileTwo,
      },
    },
  }),
  useExpansionDispatch: () => expansionDispatch,
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
  expansionDispatch.mockClear();
  selectionDispatch.mockClear();
});

test("renders", () => {
  testFileOne = ExpansionStatus.Expanded;

  const file = "test-file-one.txt";
  const reports = [{ id: "one" }, { id: "two" }] as any[];

  const { asFragment, rerender } = render(
    <ReportGroup
      section="section"
      id={file}
      name={file + " (2)"}
      reports={reports}
    />,
  );
  expect(asFragment()).toMatchSnapshot();

  testFileOne = ExpansionStatus.Collapsed;

  rerender(
    <ReportGroup
      section="section"
      id={file}
      name={file}
      reports={reports}
      icon={faFileLines}
    />,
  );
  expect(asFragment()).toMatchSnapshot();
});

test("renders solid icon on hover", () => {
  // Group collapsed
  testFileOne = ExpansionStatus.Collapsed;

  const file = "test-file-one.txt";
  const reports = [{ id: "one" }, { id: "two" }] as any[];

  const { asFragment, rerender } = render(
    <ReportGroup section="section" id={file} name={file} reports={reports} />,
  );
  expect(asFragment()).toMatchSnapshot();

  const header = screen.getByTestId(`reports-group-${file}-header`);
  expect(header).toBeInTheDocument();

  const toggle = header.querySelector(".reports-group-toggle") as Element;
  expect(toggle).toBeTruthy();
  expect(toggle).toBeInTheDocument();

  // Hover
  fireEvent.mouseEnter(toggle);

  rerender(
    <ReportGroup section="section" id={file} name={file} reports={reports} />,
  );
  expect(asFragment()).toMatchSnapshot();

  fireEvent.mouseLeave(toggle);

  rerender(
    <ReportGroup section="section" id={file} name={file} reports={reports} />,
  );
  expect(asFragment()).toMatchSnapshot();

  // Group expanded
  testFileOne = ExpansionStatus.Expanded;

  rerender(
    <ReportGroup section="section" id={file} name={file} reports={reports} />,
  );
  expect(asFragment()).toMatchSnapshot();

  // Hover
  fireEvent.mouseEnter(toggle);

  rerender(
    <ReportGroup section="section" id={file} name={file} reports={reports} />,
  );
  expect(asFragment()).toMatchSnapshot();

  fireEvent.mouseLeave(toggle);

  rerender(
    <ReportGroup section="section" id={file} name={file} reports={reports} />,
  );
  expect(asFragment()).toMatchSnapshot();
});

test("triggers scroll to source file", () => {
  const file = "test-file-one.txt";
  const reports = [{ id: "one" }, { id: "two" }] as any[];

  render(
    <ReportGroup section="section" id={file} name={file} reports={reports} />,
  );

  const header = screen.getByTestId(`reports-group-${file}-header`);
  expect(header).toBeInTheDocument();

  fireEvent.click(header);

  expect(expansionDispatch).toHaveBeenCalledTimes(1);
  expect(expansionDispatch).toHaveBeenCalledWith({
    type: "toggle",
    group: "source-file",
    id: file,
    status: ExpansionStatus.Expanded,
  });
  expect(selectionDispatch).toHaveBeenCalledTimes(1);
  expect(selectionDispatch).toHaveBeenCalledWith({ scroll: "file", file });
});

test("doesn't trigger scroll when no source file", () => {
  const file = "other";
  const reports = [{ id: "one" }, { id: "two" }] as any[];

  render(
    <ReportGroup section="section" id={file} name={file} reports={reports} />,
  );

  const header = screen.getByTestId(`reports-group-${file}-header`);
  expect(header).toBeInTheDocument();

  fireEvent.click(header);
  expect(expansionDispatch).not.toHaveBeenCalled();
  expect(selectionDispatch).not.toHaveBeenCalled();
});

test("triggers expansion", () => {
  testFileOne = ExpansionStatus.Collapsed;
  testFileTwo = ExpansionStatus.Expanded;

  const fileOne = "test-file-one.txt";
  const fileTwo = "test-file-two.txt";

  const focusAction = {
    type: "toggle",
    group: "report-group",
  };

  const { asFragment } = render(
    <>
      <ReportGroup
        section="section"
        id={fileOne}
        name={fileOne}
        reports={[{ id: "one" }, { id: "two" }] as any[]}
      />
      <ReportGroup
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

  const toggleOne = headerOne.querySelector(".reports-group-toggle") as Element;
  expect(toggleOne).toBeTruthy();
  expect(toggleOne).toBeInTheDocument();

  fireEvent.click(toggleOne);
  expect(selectionDispatch).toHaveBeenCalledTimes(1);
  expect(selectionDispatch).toHaveBeenCalledWith({
    scroll: "none",
  });
  expect(expansionDispatch).toHaveBeenCalledTimes(1);
  expect(expansionDispatch).toHaveBeenCalledWith({
    ...focusAction,
    id: `section-${fileOne}`,
    status: ExpansionStatus.Expanded,
  });

  // Trigger collapse of group two
  const headerTwo = screen.getByTestId(`reports-group-${fileTwo}-header`);
  expect(headerTwo).toBeInTheDocument();
  const toggleTwo = headerTwo.querySelector(".reports-group-toggle");
  expect(toggleTwo).toBeTruthy();
  expect(toggleTwo).toBeInTheDocument();

  fireEvent.click(toggleTwo!);
  expect(selectionDispatch).toHaveBeenCalledTimes(2);
  expect(expansionDispatch).toHaveBeenCalledTimes(2);
  expect(expansionDispatch).toHaveBeenLastCalledWith({
    ...focusAction,
    id: `section-${fileTwo}`,
    status: ExpansionStatus.Collapsed,
  });
});
