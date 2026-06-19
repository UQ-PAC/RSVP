import { act, render } from "@testing-library/react";
import { CodeRender } from "./CodeRender";

jest.mock("next/font/google", () => ({
  Roboto_Mono: jest.fn().mockReturnValue({ className: "roboto-mono" }),
}));

jest.mock("../../lib/context/SelectionContext", () => ({
  useSelection: jest.fn(() => jest.fn()),
  useSelectionDispatch: jest.fn(() => jest.fn()),
}));

jest.mock("../../lib/sources/util", () => ({
  groupReportsBySourceLine: jest.fn((file, code, reports) => {
    const reportsByLine = {};

    reports?.forEach((report, i) => {
      const n = i + 1;
      const loc = report.sourceLocations[0].location;
      if (n >= loc.startLoc.line && n < loc.endLoc.line) {
        reportsByLine[n] = [report];
      }
    });

    return { lines: code.split("\n"), reportsByLine };
  }),
}));

jest.mock("./CodeHighlight", () => jest.fn());

jest.mock("./CodeLine", () => ({
  CodeLine: jest.fn(
    ({ n, file, syntax, temporaryHighlight, children: line }) => (
      <div
        data-testid={`code-line-${n}`}
        data-syntax={syntax}
        data-temp-highlight={temporaryHighlight()}
      >
        {line}
      </div>
    ),
  ),
}));

jest.mock("./HighlightedCodeLine", () => ({
  HighlightedCodeLine: jest.fn(
    ({ n, syntax, reports, temporaryHighlight, children: line }) => (
      <div
        data-testid={`highlighted-code-line-${n}`}
        data-syntax={syntax}
        data-reports={reports.length}
        data-temp-highlight={temporaryHighlight()}
      >
        {line}
      </div>
    ),
  ),
}));

test("renders", async () => {
  const file = {
    filename: "file.cedar",
    filetype: "cedar",
    resolved: Promise.resolve({
      serverId: "123",
    }),
  } as any;

  const content = Promise.resolve(
    ["Some lines of code here", "such brilliant code", "wowee"].join("\n"),
  );

  let asFragment, rerender;

  // No reports
  await act(async () => {
    ({ asFragment, rerender } = render(
      <CodeRender file={file} content={content} />,
    ));

    await content;
  });

  expect(asFragment()).toMatchSnapshot();

  const reports = [
    {
      sourceLocations: [
        {
          location: {
            startLoc: {
              line: 1,
            },
            endLoc: {
              line: 2,
            },
          },
        },
      ],
    },
  ] as any[];

  // With a report
  await act(async () => {
    rerender(<CodeRender file={file} content={content} reports={reports} />);

    await content;
  });

  expect(asFragment()).toMatchSnapshot();
});
