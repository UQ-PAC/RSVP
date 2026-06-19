import { act, render, screen, waitFor } from "@testing-library/react";
import { ReportViewer } from "./ReportViewer";

var reportsOne = [
  {
    severity: "err",
    message: "An Error Message",
    sourceLocations: [
      {
        location: {
          source: {
            filename: "test-file-one.txt",
            filetype: "text",
            resolved: Promise.resolve({
              serverId: "123",
            }),
          },
        },
      },
    ],
  },
  {
    severity: "err",
    message: "Another Error Message",
    sourceLocations: [
      {
        location: {
          source: {
            filetype: "text",
            file: {
              name: "test-file-two.txt",
            },
            resolved: Promise.resolve({
              serverId: "456",
            }),
          },
        },
      },
    ],
  },
  {
    severity: "warn",
    message: "Heed this warning!",
    sourceLocations: [
      {
        location: {
          source: {
            filename: "test-file-one.txt",
            filetype: "text",
            resolved: Promise.resolve({
              serverId: "123",
            }),
          },
        },
      },
    ],
  },
];
var reportsTwo = [
  {
    severity: "info",
    message: "Information",
    sourceLocations: [
      {
        location: {
          source: {
            filename: "test-file-three.txt",
            filetype: "text",
            resolved: Promise.resolve({
              serverId: "987654321",
            }),
          },
        },
      },
    ],
  },
  {
    severity: "warn",
    message: "You have beeen warned.",
    sourceLocations: [
      {
        location: {
          source: {
            filename: "test-file-one.txt",
            filetype: "text",
            resolved: Promise.resolve({
              serverId: "123",
            }),
          },
        },
      },
    ],
  },
  {
    severity: "info",
    message: "Where do I belong?",
    sourceLocations: [],
  },
];

var noReports = {
  one: {},
  two: {},
};
var manyReports = {
  one: {
    reports: Promise.resolve([...reportsOne]),
  },
  two: {
    reports: Promise.resolve([...reportsTwo]),
  },
};

var verificationContext = { ...noReports };
var verificationPending;
var verificationComplete;

jest.mock("../../lib/context/VerificationContext", () => ({
  useVerification: () => verificationContext,
}));

jest.mock("../../lib/events", () => ({
  useEventListener: jest.fn((event, listener) => {
    if (event === "verificationPending") {
      verificationPending = listener;
    } else if (event === "verificationComplete") {
      verificationComplete = listener;
    }
    return jest.fn();
  }),
}));

jest.mock("./ReportSection", () => ({
  ReportSection: jest.fn(({ title, severity, reports }) => (
    <div
      data-testid={`report-section-${title}`}
      data-title={title}
      data-severity={severity}
    >
      {reports.map(([id, data], i) => (
        <div
          key={i}
          data-testid="report-group"
          data-groupid={id}
          data-filename={data.filename}
          data-filetype={data.filetype}
        >
          {data.reports.map((report, j) => {
            return (
              <div key={j} data-testid="report-item">
                {report.message}
              </div>
            );
          })}
        </div>
      ))}
    </div>
  )),
}));

beforeEach(() => {
  verificationContext = { ...noReports };
});

jest.mock("../shared/ProgressSpinner", () => ({
  ProgressSpinner: jest.fn(({ text }) => (
    <div data-testid="progress-spinner" data-text={text}></div>
  )),
}));

test("renders", async () => {
  const { asFragment, rerender } = render(<ReportViewer />);
  expect(asFragment()).toMatchSnapshot();

  act(() => {
    verificationContext = { ...manyReports };
    rerender(<ReportViewer />);
  });

  await waitFor(() => {
    expect(screen.getByTestId("report-section-Errors")).toBeInTheDocument();
  });

  expect(asFragment()).toMatchSnapshot();
});

test("handles verification events", async () => {
  const { asFragment, rerender } = render(<ReportViewer />);

  act(() => {
    verificationContext = { ...manyReports };
    rerender(<ReportViewer />);
  });

  await waitFor(() => {
    expect(screen.getByTestId("report-section-Errors")).toBeInTheDocument();
  });

  // Simulate verification pending
  expect(verificationPending).toBeDefined();
  act(() => {
    verificationPending();
    rerender(<ReportViewer />);
  });

  expect(asFragment()).toMatchSnapshot();

  // Simulate verification complete
  expect(verificationComplete).toBeDefined();
  act(() => {
    verificationComplete();
    rerender(<ReportViewer />);
  });

  await waitFor(() => {
    expect(screen.getByTestId("report-section-Errors")).toBeInTheDocument();
  });

  expect(asFragment()).toMatchSnapshot();
});
