import { render, screen } from "@testing-library/react";

import { ExpansionStatus } from "../../lib/context/ExpansionContext";
import { ReportGroupListing, ReportSection } from "./ReportSection";

var expansionDispatch = jest.fn();
var toggleAll;

jest.mock("../../lib/context/ExpansionContext", () => ({
  ...jest.requireActual("../../lib/context/ExpansionContext"),
  useExpansionDispatch: () => expansionDispatch,
}));

jest.mock("../../lib/fa-util", () => ({
  getFileIcon: jest.fn((filetype) => `${filetype}-icon`),
}));

jest.mock("../shared/ToggleAll", () => ({
  ToggleAll: jest.fn(({ name, toggle }) => {
    toggleAll = toggle;
    return <div data-testid="toggle-all" data-name={name} />;
  }),
}));

jest.mock("./ReportGroup", () => ({
  ReportGroup: jest.fn(({ name, id, section, reports, icon }) => (
    <div
      data-testid={`reports-group-${name.replaceAll(" ", "-")}`}
      data-name={name}
      data-id={id}
      data-section={section}
      data-reportcount={reports.length}
      data-icon={icon}
    />
  )),
}));

beforeEach(() => {
  expansionDispatch.mockClear();
});

test("renders", () => {
  const { asFragment, rerender } = render(
    <ReportSection
      title="Silly Mistakes"
      severity="warn"
      reports={
        [
          ["group one", { filename: "group one", reports: [{}, {}] }],
          ["group two", { filename: "group two", reports: [{}, {}, {}] }],
        ] as ReportGroupListing[]
      }
    />,
  );
  expect(asFragment()).toMatchSnapshot();

  rerender(
    <ReportSection
      title="Silly Mistakes"
      severity="warn"
      reports={
        [
          [
            "group five",
            { filename: "group five", filetype: "cedar", reports: [] },
          ],
          [
            "group two",
            {
              filename: "group two",
              filetype: "cedarschema",
              reports: [{}, {}, {}],
            },
          ],
        ] as ReportGroupListing[]
      }
    />,
  );
  expect(asFragment()).toMatchSnapshot();
});

test("toggles group expansions", () => {
  const focusAction = {
    type: "toggle",
    group: "report-group",
  };

  const { asFragment } = render(
    <ReportSection
      title="Expanded Section"
      severity="err"
      reports={
        [
          ["group one", { filename: "group one", reports: [{}, {}] }],
          ["group two", { filename: "group two", reports: [{}, {}, {}] }],
          ["group three", { filename: "group three", reports: [{}] }],
        ] as ReportGroupListing[]
      }
    />,
  );
  expect(asFragment()).toMatchSnapshot();

  const toggle = screen.getByTestId("toggle-all");
  expect(toggle).toBeInTheDocument();

  expect(toggleAll).toBeDefined();

  toggleAll(ExpansionStatus.Collapsed);

  expect(expansionDispatch).toHaveBeenCalledTimes(3);
  expect(expansionDispatch).toHaveBeenNthCalledWith(1, {
    ...focusAction,
    id: "err-group one",
    status: ExpansionStatus.Collapsed,
  });
  expect(expansionDispatch).toHaveBeenNthCalledWith(2, {
    ...focusAction,
    id: "err-group two",
    status: ExpansionStatus.Collapsed,
  });
  expect(expansionDispatch).toHaveBeenNthCalledWith(3, {
    ...focusAction,
    id: "err-group three",
    status: ExpansionStatus.Collapsed,
  });

  toggleAll(ExpansionStatus.Expanded);
  expect(expansionDispatch).toHaveBeenCalledTimes(6);

  expect(expansionDispatch).toHaveBeenNthCalledWith(4, {
    ...focusAction,
    id: "err-group one",
    status: ExpansionStatus.Expanded,
  });
  expect(expansionDispatch).toHaveBeenNthCalledWith(5, {
    ...focusAction,
    id: "err-group two",
    status: ExpansionStatus.Expanded,
  });
  expect(expansionDispatch).toHaveBeenNthCalledWith(6, {
    ...focusAction,
    id: "err-group three",
    status: ExpansionStatus.Expanded,
  });
});
