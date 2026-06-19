import { act, fireEvent, render, screen } from "@testing-library/react";
import { ExpansionStatus } from "../../lib/context/ExpansionContext";
import { Report, SourceLoc } from "../../lib/types";
import { ReportItem } from "./ReportItem";

var expansionDispatch = jest.fn();
var selectionDispatch = jest.fn();
var selectedReport = "";
var hoveredReport = "";
var scrollSelection = "none";
var selectedLoc = "";

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

jest.mock("../../lib/context/ExpansionContext", () => ({
  ...jest.requireActual("../../lib/context/ExpansionContext"),
  useExpansionDispatch: () => expansionDispatch,
}));

jest.mock("../../lib/context/SelectionContext", () => ({
  useSelection: () => ({
    selected: selectedReport,
    hovered: hoveredReport,
    scroll: scrollSelection,
    loc: selectedLoc,
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
  expansionDispatch.mockClear();
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
  act(() => {
    selectedReport = "123";
  });

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

  act(() => {
    hoveredReport = "123";
  });

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

  act(() => {
    scrollSelection = "report";
  });

  rerender(<ReportItem report={report} />);

  expect(scrollIntoView).toHaveBeenCalledTimes(1);

  act(() => {
    selectedReport = "456";
  });

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
  act(() => {
    selectedReport = "123";
  });
  rerender(<ReportItem report={report} />);
  fireEvent.click(element);

  expect(expansionDispatch).not.toHaveBeenCalled();
  expect(selectionDispatch).toHaveBeenCalledTimes(2);
  expect(selectionDispatch).toHaveBeenNthCalledWith(2, {
    selected: "",
    scroll: "none",
  });

  // Select triggers focus on source location
  act(() => {
    selectedReport = "";
  });
  report.sourceLocations.push({
    location: {
      file: "some-file.txt",
    },
  } as any);

  rerender(<ReportItem report={report} />);
  fireEvent.click(element);

  expect(selectionDispatch).toHaveBeenCalledTimes(3);
  expect(expansionDispatch).toHaveBeenCalledTimes(1);
  expect(expansionDispatch).toHaveBeenCalledWith({
    type: "toggle",
    group: "source-file",
    id: "some-file.txt",
    status: ExpansionStatus.Expanded,
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
  act(() => {
    selectedReport = "123";
  });
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
        message: "source location message",
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
      {
        message: "a mystery!",
        location: {
          file: "unresolved-file.txt",
          offset: 350,
          len: 2,
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
    act(() => {
      selectedReport = "123";
    });

    rerender(<ReportItem report={report} />);
    expect(asFragment()).toMatchSnapshot();

    // Hover over location
    act(() => {
      selectedLoc = "123:test-file.txt:100:10-ident";
    });

    rerender(<ReportItem report={report} />);
    expect(asFragment()).toMatchSnapshot();
  });

  test("triggers source location selection", async () => {
    act(() => {
      selectedReport = "123";
    });

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

    expect(expansionDispatch).toHaveBeenCalledTimes(1);
    expect(expansionDispatch).toHaveBeenCalledWith({
      type: "toggle",
      group: "source-file",
      id: "test-file.txt",
      status: ExpansionStatus.Expanded,
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

    expect(expansionDispatch).toHaveBeenCalledTimes(2);
    expect(expansionDispatch).toHaveBeenNthCalledWith(2, {
      type: "toggle",
      group: "source-file",
      id: "another-file.txt",
      status: ExpansionStatus.Expanded,
    });
  });

  test("triggers source location hover", () => {
    act(() => {
      selectedReport = "123";
    });

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
