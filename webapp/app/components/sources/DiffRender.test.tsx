/* eslint-disable @typescript-eslint/no-explicit-any */
/* eslint-disable no-var */
import { render, screen, waitFor } from "@testing-library/react";
import { AnalysisGroup, VerificationFile } from "../../lib/types";
import { DiffRender } from "./DiffRender";

var analysisGroup: AnalysisGroup = {
  name: "",
  files: [],
  diffs: {},
  verifyRequested: false,
  verifyPending: false,
};

var diff = Promise.resolve("");
var impact = undefined;

jest.mock("next/font/google", () => ({
  Roboto_Mono: jest.fn().mockReturnValue({ className: "roboto-mono" }),
}));

jest.mock("diff2html/lib/ui/js/diff2html-ui-slim.js", () => ({
  Diff2HtmlUI: jest
    .fn()
    .mockImplementation((ref: HTMLDivElement, diff: string) => ({
      draw: jest.fn().mockImplementation(() => {
        ref.innerText = diff;
        ref.setAttribute("data-testid", "diff-render");
      }),
    })),
}));

jest.mock("../../lib/context/AnalysisGroupContext", () => ({
  useAnalysisGroup: jest.fn(() => analysisGroup),
  useAnalysisGroupDispatch: jest.fn(() => jest.fn()),
}));

jest.mock("../../lib/requests", () => ({
  diff: jest.fn(() => diff),
  impact: jest.fn(() => impact),
}));

jest.mock("../shared/ProgressSpinner", () => ({
  ProgressSpinner: jest.fn((text) => (
    <div data-testid="progress-spinner" data-text={text}></div>
  )),
}));

const original: VerificationFile = { file: { name: "original-file" } } as any;
const updated: VerificationFile = { file: { name: "updated-file" } } as any;
const originalId = "1234";
const updatedId = "5678";

test("renders diff", async () => {
  diff = Promise.resolve("Fake diff string");
  const { asFragment, rerender } = render(
    <DiffRender
      original={original}
      updated={updated}
      originalId={originalId}
      updatedId={updatedId}
    />,
  );

  expect(asFragment()).toMatchSnapshot();

  await waitFor(() => {
    expect(screen.getByTestId("diff-render")).toBeInTheDocument();
  });

  rerender(
    <DiffRender
      original={original}
      updated={updated}
      originalId={originalId}
      updatedId={updatedId}
    />,
  );
  expect(asFragment()).toMatchSnapshot();
});

test("renders empty", async () => {
  diff = Promise.resolve("");
  const { asFragment, rerender } = render(
    <DiffRender
      original={original}
      updated={updated}
      originalId={originalId}
      updatedId={updatedId}
    />,
  );

  expect(asFragment()).toMatchSnapshot();

  await waitFor(() => {
    expect(screen.getByTestId("empty-diff-render")).toBeInTheDocument();
  });

  rerender(
    <DiffRender
      original={original}
      updated={updated}
      originalId={originalId}
      updatedId={updatedId}
    />,
  );
  expect(asFragment()).toMatchSnapshot();
});
