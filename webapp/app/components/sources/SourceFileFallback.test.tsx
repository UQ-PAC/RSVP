import { act, fireEvent, render, screen } from "@testing-library/react";

import { SourceFileFallback } from "./SourceFileFallback";

jest.mock("@fortawesome/react-fontawesome", () => ({
  FontAwesomeIcon: jest.fn(({ icon }) => (
    <div data-testid="font-awesome-icon" data-icon={icon} />
  )),
}));

jest.mock("../../lib/fa-util", () => ({
  getFileIcon: jest.fn((filetype) => `${filetype}-icon`),
  getExpandIcon: jest.fn((hover) => `${hover ? "hovered-" : ""}icon`),
}));

jest.mock("./CodeRender", () => ({
  CodeRender: jest.fn(({ file, reports }) => (
    <div
      data-testid="code-render"
      data-file={file.filename}
      data-reports={reports.length}
    />
  )),
}));

test("renders", () => {
  const file = {
    filetype: "text",
    filename: "file.txt",
  } as any;

  const { asFragment } = render(<SourceFileFallback file={file} />);
  expect(asFragment()).toMatchSnapshot();
});

test("toggles icon on hover", () => {
  const file = {
    filetype: "text",
    filename: "file.txt",
  } as any;

  const { asFragment, rerender } = render(<SourceFileFallback file={file} />);
  expect(asFragment()).toMatchSnapshot();

  const header = screen.getByTestId("source-fallback-header");
  expect(header).toBeInTheDocument();

  act(() => {
    fireEvent.mouseOver(header);
    rerender(<SourceFileFallback file={file} />);
  });

  expect(asFragment()).toMatchSnapshot();

  act(() => {
    fireEvent.mouseOut(header);
    rerender(<SourceFileFallback file={file} />);
  });

  expect(asFragment()).toMatchSnapshot();
});
