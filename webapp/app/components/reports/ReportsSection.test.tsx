/* eslint-disable no-var */
import { render, screen } from "@testing-library/react";

import { ExpansionState } from "../../lib/context/FocusContext";
import { ReportGroup, ReportsSection } from "./ReportsSection";

var focusDispatch = jest.fn();
var toggleAll;

jest.mock("../../lib/context/FocusContext", () => ({
  ...jest.requireActual("../../lib/context/FocusContext"),
  useFocusDispatch: () => focusDispatch,
}));

jest.mock("../shared/ToggleAll", () => ({
  ToggleAll: jest.fn(({ name, toggle }) => {
    toggleAll = toggle;
    return <div data-testid="toggle-all" data-name={name} />;
  }),
}));

jest.mock("./ReportsGroup", () => ({
  ReportsGroup: jest.fn(({ name, section, reports }) => (
    <div
      data-testid={`reports-section-${name.replaceAll(" ", "-")}`}
      data-name={name}
      data-section={section}
      data-reportcount={reports.length}
    />
  )),
}));

beforeEach(() => {
  focusDispatch.mockClear();
});

test("renders", () => {
  const { asFragment, rerender } = render(
    <ReportsSection
      title="Silly Mistakes"
      severity="warn"
      reports={
        [
          ["group one", { filename: "group one", reports: [{}, {}] }],
          ["group two", { filename: "group two", reports: [{}, {}, {}] }],
        ] as ReportGroup[]
      }
    />,
  );
  expect(asFragment()).toMatchSnapshot();

  rerender(
    <ReportsSection
      title="Silly Mistakes"
      severity="warn"
      reports={
        [
          ["group five", { filename: "group five", reports: [] }],
          ["group two", { filename: "group two", reports: [{}, {}, {}] }],
        ] as ReportGroup[]
      }
    />,
  );
  expect(asFragment()).toMatchSnapshot();
});

test("toggles group expansions", () => {
  const focusAction = {
    type: "focus",
    target: "report-group",
  };

  const { asFragment } = render(
    <ReportsSection
      title="Expanded Section"
      severity="err"
      reports={
        [
          ["group one", { filename: "group one", reports: [{}, {}] }],
          ["group two", { filename: "group two", reports: [{}, {}, {}] }],
          ["group three", { filename: "group three", reports: [{}] }],
        ] as ReportGroup[]
      }
    />,
  );
  expect(asFragment()).toMatchSnapshot();

  const toggle = screen.getByTestId("toggle-all");
  expect(toggle).toBeInTheDocument();

  expect(toggleAll).toBeDefined();

  toggleAll(ExpansionState.Collapsed);

  expect(focusDispatch).toHaveBeenCalledTimes(3);
  expect(focusDispatch).toHaveBeenNthCalledWith(1, {
    ...focusAction,
    focus: { key: "err-group one", value: ExpansionState.Collapsed },
  });
  expect(focusDispatch).toHaveBeenNthCalledWith(2, {
    ...focusAction,
    focus: { key: "err-group two", value: ExpansionState.Collapsed },
  });
  expect(focusDispatch).toHaveBeenNthCalledWith(3, {
    ...focusAction,
    focus: { key: "err-group three", value: ExpansionState.Collapsed },
  });

  toggleAll(ExpansionState.Expanded);
  expect(focusDispatch).toHaveBeenCalledTimes(6);

  expect(focusDispatch).toHaveBeenNthCalledWith(4, {
    ...focusAction,
    focus: { key: "err-group one", value: ExpansionState.Expanded },
  });
  expect(focusDispatch).toHaveBeenNthCalledWith(5, {
    ...focusAction,
    focus: { key: "err-group two", value: ExpansionState.Expanded },
  });
  expect(focusDispatch).toHaveBeenNthCalledWith(6, {
    ...focusAction,
    focus: { key: "err-group three", value: ExpansionState.Expanded },
  });
});
