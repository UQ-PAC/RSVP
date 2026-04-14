import { render, screen } from "@testing-library/react";
import Home from "./page";

jest.mock("next/font/google", () => ({
  Roboto_Mono: jest.fn().mockReturnValue({ className: "" }),
  Lexend_Deca: jest.fn().mockReturnValue({ className: "" }),
  Lexend_Giga: jest.fn().mockReturnValue({ className: "" }),
}));

jest.mock("./components/providers/ContextProvider", () => ({
  ContextProvider: jest.fn(({ children }) => (
    <div data-testid="context-provider">{children}</div>
  )),
}));
jest.mock("./components/sources/SourceFileViewer", () => ({
  SourceFileViewer: jest.fn(() => <div data-testid="source-file-viewer" />),
}));
jest.mock("./components/header/Header", () => ({
  Header: jest.fn(({ heading, subheading }) => (
    <div
      data-testid="header"
      data-heading={heading}
      data-subheading={subheading}
    />
  )),
}));
jest.mock("./components/Drawer", () => ({
  Drawer: jest.fn(({ title, side, children }) => (
    <div data-testid={`drawer-${side}`} data-title={title} data-side={side}>
      {children}
    </div>
  )),
}));
jest.mock("./components/reports/ReportViewer", () => ({
  ReportViewer: jest.fn(() => <div data-testid="report-viewer" />),
}));
jest.mock("./components/upload/FileUploader", () => ({
  FileUploader: jest.fn(() => <div data-testid="file-uploader" />),
}));

describe("App", () => {
  test("renders", () => {
    const { asFragment } = render(<Home />);
    expect(asFragment()).toMatchSnapshot();
    const header = screen.getByTestId("header");
    expect(header).toHaveAttribute("data-subheading", "Policy Verification");
  });
});
