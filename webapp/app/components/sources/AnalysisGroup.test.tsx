import { act, render, screen } from "@testing-library/react";
import { ExpansionStatus } from "../../lib/context/ExpansionContext";
import { AnalysisGroup } from "./AnalysisGroup";

// Mock analysis group
var name = "name";
var empty = {
  name,
  files: [],
};

var withFiles = {
  name,
  files: [
    {
      original: {
        filename: "file-one.txt",
        file: {
          lastModified: 1,
          size: 100,
        },
      },
    },
    {
      original: {
        filename: "file-two.txt",
        file: {
          lastModified: 2,
          size: 200,
        },
      },
    },
    {
      original: {
        filename: "file-three.txt",
        file: {
          lastModified: 3,
          size: 300,
        },
      },
    },
  ],
};

var analysisGroup;

// Mock selection context
var noSelection = {
  scroll: "none",
};
var groupSelected = {
  scroll: "group",
  group: name,
};

var selectionContext;

// Mock toggle functions
var toggleAllFunc;
var toggleCallback = jest.fn();

jest.mock("./Fallback", () => ({
  Fallback: jest.fn(({ instruction, target }) => (
    <div
      data-testid="fallback"
      data-instruction={instruction}
      data-target={target}
    />
  )),
}));

jest.mock("./SourceFile", () => ({
  SourceFile: jest.fn(({ source, setExpansionCallback }) => {
    setExpansionCallback(toggleCallback);
    return (
      <div data-testid="source-file" data-source={source.original.filename} />
    );
  }),
}));

jest.mock("../../lib/context/AnalysisGroupContext", () => ({
  useAnalysisGroup: jest.fn(() => analysisGroup),
  useAnalysisGroupDispatch: jest.fn(() => jest.fn()),
}));

jest.mock("../../lib/context/SelectionContext", () => ({
  useSelection: jest.fn(() => selectionContext),
}));

jest.mock("../shared/ToggleAll", () => ({
  ToggleAll: jest.fn(({ name, toggle }) => {
    toggleAllFunc = toggle;
    return <div data-testid="toggle-all" data-name={name} />;
  }),
}));

beforeEach(() => {
  toggleCallback.mockClear();
  analysisGroup = withFiles;
  selectionContext = noSelection;
  toggleAllFunc = undefined;
});

test("renders", () => {
  // No sources, render fallback
  analysisGroup = empty;

  const { asFragment, rerender } = render(<AnalysisGroup />);
  expect(asFragment()).toMatchSnapshot();

  // Render source files
  analysisGroup = withFiles;
  rerender(<AnalysisGroup />);
  expect(asFragment()).toMatchSnapshot();
});

test("toggles all file expansions", () => {
  render(<AnalysisGroup />);

  expect(toggleAllFunc).toBeDefined();
  toggleAllFunc(ExpansionStatus.Expanded);

  expect(toggleCallback).toHaveBeenCalledTimes(3);
  expect(toggleCallback).toHaveBeenCalledWith(ExpansionStatus.Expanded);
  expect(toggleCallback).not.toHaveBeenCalledWith(ExpansionStatus.Collapsed);

  toggleCallback.mockClear();

  toggleAllFunc(ExpansionStatus.Collapsed);
  expect(toggleCallback).toHaveBeenCalledTimes(3);
  expect(toggleCallback).not.toHaveBeenCalledWith(ExpansionStatus.Expanded);
  expect(toggleCallback).toHaveBeenCalledWith(ExpansionStatus.Collapsed);
});

test("scrolls selected policy into view", () => {
  const { rerender } = render(<AnalysisGroup />);

  const container = screen.getByTestId("source-analysis-group");
  expect(container).toBeInTheDocument();
  container.scrollIntoView = jest.fn();

  act(() => {
    selectionContext = groupSelected;
    rerender(<AnalysisGroup />);
  });

  expect(container.scrollIntoView).toHaveBeenCalledTimes(1);
});
