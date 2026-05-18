/* eslint-disable @typescript-eslint/no-explicit-any */
/* eslint-disable no-var */
import { fireEvent, render, screen } from "@testing-library/react";
import { ExpansionState } from "../../lib/context/FocusContext";
import { Report, SourceLoc } from "../../lib/types";
import { ReportItem } from "./ReportItem";

var focusDispatch = jest.fn();
var selectionDispatch = jest.fn();
var selectedReport = "";
var hoveredReport = "";
var scrollSelection = "none";

jest.mock("@fortawesome/react-fontawesome", () => ({
  FontAwesomeIcon: jest.fn(({ icon }) => (
    <div data-testid="font-awesome-icon" data-icon={icon} />
  )),
}));

jest.mock("@fortawesome/free-solid-svg-icons", () => ({
  faCaretDown: "faCaretDown",
  faCaretUp: "faCaretUp",
  faCircleExclamation: "faCircleExclamation",
  faCircleInfo: "faCircleInfo",
  faCircleXmark: "faCircleXmark",
}));

jest.mock("../../lib/context/FocusContext", () => ({
  ...jest.requireActual("../../lib/context/FocusContext"),
  useFocusDispatch: () => focusDispatch,
}));

jest.mock("../../lib/context/SelectionContext", () => ({
  useSelection: () => ({
    selected: selectedReport,
    hovered: hoveredReport,
    scroll: scrollSelection,
  }),
  useSelectionDispatch: () => selectionDispatch,
}));

jest.mock("../../lib/util", () => ({
  getSourceIdentifier: jest.fn(
    (loc) => `${loc.file}:${loc.offset}:${loc.len}-ident`,
  ),
  getSourceStr: jest.fn((loc) => `${loc.offset}:${loc.len}`),
}));

beforeEach(() => {
  focusDispatch.mockClear();
  selectionDispatch.mockClear();
  selectedReport = "";
  hoveredReport = "";
  scrollSelection = "none";
});

test("renders", () => {
  const locations: { message: string; location: SourceLoc }[] = [
    {
      message: "location message",
      location: {
        file: "test-file.txt",
        offset: 100,
        len: 10,
      },
    },
  ];

  // Error, no detail
  const { asFragment, rerender } = render(
    <ReportItem
      report={{
        id: "123",
        severity: "err",
        sourceLocations: locations,
        message: "Test Report",
      }}
    />,
  );
  expect(asFragment()).toMatchSnapshot();

  // Warning, with detail
  // Deselected
  rerender(
    <ReportItem
      report={{
        id: "123",
        severity: "warn",
        sourceLocations: locations,
        message: "Test Report",
        messageDetail: "Details about the report",
      }}
    />,
  );
  expect(asFragment()).toMatchSnapshot();

  // Selected
  selectedReport = "123";

  rerender(
    <ReportItem
      report={{
        id: "123",
        severity: "warn",
        sourceLocations: locations,
        message: "Test Report",
        messageDetail: "Details about the report",
      }}
    />,
  );
  expect(asFragment()).toMatchSnapshot();

  hoveredReport = "123";

  rerender(
    <ReportItem
      report={{
        id: "123",
        severity: "warn",
        sourceLocations: locations,
        message: "Test Report",
        messageDetail: "Details about the report",
      }}
    />,
  );
  expect(asFragment()).toMatchSnapshot();
});

test("handles no source location", () => {
  const { asFragment } = render(
    <ReportItem
      report={{
        id: "123",
        severity: "info",
        sourceLocations: [],
        message: "Test Report",
      }}
    />,
  );
  expect(asFragment()).toMatchSnapshot();
});

test("scrolls into view", async () => {
  selectedReport = "123";
  const report: Report = {
    id: "123",
    severity: "info",
    sourceLocations: [],
    message: "Test Report",
  };

  const { rerender } = render(<ReportItem report={report} />);

  const element = screen.getByTestId("report-item");
  expect(element).toBeInTheDocument();

  const scrollIntoView = jest.fn();
  element.scrollIntoView = scrollIntoView;

  scrollSelection = "report";

  rerender(<ReportItem report={report} />);

  expect(scrollIntoView).toHaveBeenCalledTimes(1);

  selectedReport = "456";

  rerender(<ReportItem report={report} />);

  expect(scrollIntoView).toHaveBeenCalledTimes(1);
});

test("triggers selection", async () => {
  const report: Report = {
    id: "123",
    severity: "info",
    sourceLocations: [],
    message: "Test Report",
  };

  const { rerender } = render(<ReportItem report={report} />);

  const element = screen.getByTestId("report-item");
  expect(element).toBeInTheDocument();

  // Select
  fireEvent.click(element);

  expect(selectionDispatch).toHaveBeenCalledTimes(1);
  expect(selectionDispatch).toHaveBeenCalledWith({
    selected: "123",
    scroll: "source",
  });

  // Deselect
  selectedReport = "123";
  rerender(<ReportItem report={report} />);
  fireEvent.click(element);

  expect(focusDispatch).not.toHaveBeenCalled();
  expect(selectionDispatch).toHaveBeenCalledTimes(2);
  expect(selectionDispatch).toHaveBeenNthCalledWith(2, {
    selected: "",
    scroll: "none",
  });

  // Select triggers focus on source location
  selectedReport = "";
  report.sourceLocations.push({
    location: {
      file: "some-file.txt",
    },
  } as any);

  rerender(<ReportItem report={report} />);
  fireEvent.click(element);

  expect(selectionDispatch).toHaveBeenCalledTimes(3);
  expect(focusDispatch).toHaveBeenCalledTimes(1);
  expect(focusDispatch).toHaveBeenCalledWith({
    type: "focus",
    target: "source-file",
    focus: { key: "some-file.txt", value: ExpansionState.Expanded },
  });
});

test("triggers hover", () => {
  const report: Report = {
    id: "123",
    severity: "info",
    sourceLocations: [],
    message: "Test Report",
  };

  const { rerender } = render(<ReportItem report={report} />);

  const element = screen.getByTestId("report-item");
  expect(element).toBeInTheDocument();

  // Hover
  fireEvent.mouseOver(element);

  expect(selectionDispatch).toHaveBeenCalledTimes(1);
  expect(selectionDispatch).toHaveBeenCalledWith({
    hovered: "123",
    scroll: "none",
    loc: undefined,
  });

  // Stop hover
  selectedReport = "123";
  rerender(<ReportItem report={report} />);
  fireEvent.mouseOut(element);

  expect(selectionDispatch).toHaveBeenCalledTimes(2);
  expect(selectionDispatch).toHaveBeenNthCalledWith(2, {
    hovered: "",
    scroll: "none",
    loc: undefined,
  });
});

describe("multiple source locations", () => {
  const report: Report = {
    id: "123",
    severity: "info",
    sourceLocations: [
      {
        location: {
          file: "test-file.txt",
          offset: 100,
          len: 10,
        },
      },
      {
        location: {
          file: "another-file.txt",
          offset: 10,
          len: 50,
          source: {
            filename: "another-file.txt",
          },
        },
      },
    ] as any[],
    message: "Test Report",
  };

  test("renders", () => {
    // Deselected
    const { asFragment, rerender } = render(<ReportItem report={report} />);
    expect(asFragment()).toMatchSnapshot();

    // Selected
    selectedReport = "123";

    rerender(<ReportItem report={report} />);
    expect(asFragment()).toMatchSnapshot();
  });

  test("triggers source location selection", async () => {
    selectedReport = "123";

    render(<ReportItem report={report} />);

    // Select first source location
    const locOne = screen.getByTestId(
      "report-item-source-location-test-file.txt:100:10-ident",
    );
    expect(locOne).toBeInTheDocument();
    fireEvent.click(locOne);

    expect(selectionDispatch).toHaveBeenCalledTimes(1);
    expect(selectionDispatch).toHaveBeenCalledWith({
      scroll: "source",
      loc: "123:test-file.txt:100:10-ident",
    });

    expect(focusDispatch).toHaveBeenCalledTimes(1);
    expect(focusDispatch).toHaveBeenCalledWith({
      type: "focus",
      target: "source-file",
      focus: { key: "test-file.txt", value: ExpansionState.Expanded },
    });

    // Select second source location
    const locTwo = screen.getByTestId(
      "report-item-source-location-another-file.txt:10:50-ident",
    );
    expect(locTwo).toBeInTheDocument();
    fireEvent.click(locTwo);

    expect(selectionDispatch).toHaveBeenCalledTimes(2);
    expect(selectionDispatch).toHaveBeenNthCalledWith(2, {
      scroll: "source",
      loc: "123:another-file.txt:10:50-ident",
    });

    expect(focusDispatch).toHaveBeenCalledTimes(2);
    expect(focusDispatch).toHaveBeenNthCalledWith(2, {
      type: "focus",
      target: "source-file",
      focus: { key: "another-file.txt", value: ExpansionState.Expanded },
    });
  });

  test("triggers source location hover", () => {
    selectedReport = "123";

    render(<ReportItem report={report} />);

    // Hover
    const locOne = screen.getByTestId(
      "report-item-source-location-test-file.txt:100:10-ident",
    );
    expect(locOne).toBeInTheDocument();
    fireEvent.mouseOver(locOne);

    expect(selectionDispatch).toHaveBeenCalledTimes(1);
    expect(selectionDispatch).toHaveBeenCalledWith({
      scroll: "none",
      hovered: "",
      loc: "123:test-file.txt:100:10-ident",
    });

    // Mouse out
    fireEvent.mouseOut(locOne);

    expect(selectionDispatch).toHaveBeenCalledTimes(2);
    expect(selectionDispatch).toHaveBeenNthCalledWith(2, {
      scroll: "none",
      hovered: "",
    });
  });
});
