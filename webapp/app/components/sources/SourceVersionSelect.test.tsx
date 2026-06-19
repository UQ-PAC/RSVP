import { fireEvent, render, screen } from "@testing-library/react";
import { SourceVersionSelect } from "./SourceVersionSelect";

jest.mock("@fortawesome/free-solid-svg-icons", () => ({
  faCodeCompare: "faCodeCompare",
  faFileCircleMinus: "faFileCircleMinus",
  faFileCirclePlus: "faFileCirclePlus",
}));

jest.mock("@fortawesome/react-fontawesome", () => ({
  FontAwesomeIcon: jest.fn(({ icon }) => (
    <div data-testid="font-awesome-icon" data-icon={icon} />
  )),
}));

var tabIdx = 0;

jest.mock("../shared/tabs/Tabs", () => ({
  Tabs: jest.fn(
    ({ options, overrideOption, onSelect, tabContent, tabTheme }) => (
      <div data-testid={`tabs-${++tabIdx}`}>
        {[...Array(options).keys()].map((i) => (
          <div
            key={i}
            data-testid={`tab-${i + 1}`}
            data-theme={tabTheme?.(i)}
            data-selected={i === overrideOption ? true : undefined}
            onClick={() => onSelect(i)}
          >
            {tabContent(i)}
          </div>
        ))}
      </div>
    ),
  ),
}));

beforeEach(() => {
  tabIdx = 0;
});

test("renders", () => {
  const versions = [
    { id: "1", name: "One" },
    { id: "2", name: "Two" },
    { id: "3", name: "Three" },
  ];

  // First version is selected
  const { asFragment, rerender } = render(
    <SourceVersionSelect
      versions={versions}
      selected={{ original: "1" }}
      expanded={true}
      tabIcon={"tabIcon" as any}
      setVersion={jest.fn()}
      onClickHeader={jest.fn()}
      onMouseOverHeader={jest.fn()}
      onMouseOutHeader={jest.fn()}
    />,
  );

  expect(asFragment()).toMatchSnapshot();

  // Select last version
  rerender(
    <SourceVersionSelect
      versions={versions}
      selected={{ original: "3" }}
      expanded={true}
      tabIcon={"tabIcon" as any}
      setVersion={jest.fn()}
      onClickHeader={jest.fn()}
      onMouseOverHeader={jest.fn()}
      onMouseOutHeader={jest.fn()}
    />,
  );

  expect(asFragment()).toMatchSnapshot();

  // Select comparison
  rerender(
    <SourceVersionSelect
      versions={versions}
      selected={{ original: "2", updated: "3" }}
      expanded={true}
      tabIcon={"tabIcon" as any}
      setVersion={jest.fn()}
      onClickHeader={jest.fn()}
      onMouseOverHeader={jest.fn()}
      onMouseOutHeader={jest.fn()}
    />,
  );

  expect(asFragment()).toMatchSnapshot();

  // Collapse bottom row of tabs
  rerender(
    <SourceVersionSelect
      versions={versions}
      selected={{ original: "2", updated: "3" }}
      expanded={false}
      tabIcon={"tabIcon" as any}
      setVersion={jest.fn()}
      onClickHeader={jest.fn()}
      onMouseOverHeader={jest.fn()}
      onMouseOutHeader={jest.fn()}
    />,
  );

  expect(asFragment()).toMatchSnapshot();
});

test("selects versions", () => {
  const setVersion = jest.fn();

  const { rerender } = render(
    <SourceVersionSelect
      versions={[
        { id: "1", name: "One" },
        { id: "2", name: "Two" },
        { id: "3", name: "Three" },
      ]}
      selected={{ original: "1", updated: "2" }}
      expanded={true}
      tabIcon={"tabIcon" as any}
      setVersion={setVersion}
      onClickHeader={jest.fn()}
      onMouseOverHeader={jest.fn()}
      onMouseOutHeader={jest.fn()}
    />,
  );

  const first = screen.getAllByTestId("tab-1");
  expect(first).toHaveLength(3);

  const second = screen.getAllByTestId("tab-2");
  expect(second).toHaveLength(3);

  const third = screen.getAllByTestId("tab-3");
  expect(third).toHaveLength(1);

  const compare = screen.getAllByTestId("tab-4");
  expect(compare).toHaveLength(1);

  // Select version 1
  expect(first[0]).toBeInTheDocument();
  fireEvent.click(first[0]);
  expect(setVersion).toHaveBeenCalledTimes(1);
  expect(setVersion).toHaveBeenLastCalledWith("1");

  // Select version 3
  expect(third[0]).toBeInTheDocument();
  fireEvent.click(third[0]);
  expect(setVersion).toHaveBeenCalledTimes(2);
  expect(setVersion).toHaveBeenLastCalledWith("3");

  // Set left of diff to version 2 (increases right diff to be more recent)
  expect(second[1]).toBeInTheDocument();
  fireEvent.click(second[1]);
  expect(setVersion).toHaveBeenCalledTimes(3);
  expect(setVersion).toHaveBeenLastCalledWith("2", "3");

  // Set left of diff to version 1 (persists right diff)
  expect(first[1]).toBeInTheDocument();
  fireEvent.click(first[1]);
  expect(setVersion).toHaveBeenCalledTimes(4);
  expect(setVersion).toHaveBeenLastCalledWith("1", "2");

  // Set right of diff to version 2
  expect(first[2]).toBeInTheDocument();
  fireEvent.click(first[2]);
  expect(setVersion).toHaveBeenCalledTimes(5);
  expect(setVersion).toHaveBeenLastCalledWith("1", "2");

  // Selecting comparison selects default diff
  rerender(
    <SourceVersionSelect
      versions={[
        { id: "1", name: "One" },
        { id: "2", name: "Two" },
        { id: "3", name: "Three" },
      ]}
      selected={{ original: "3" }}
      expanded={true}
      tabIcon={"tabIcon" as any}
      setVersion={setVersion}
      onClickHeader={jest.fn()}
      onMouseOverHeader={jest.fn()}
      onMouseOutHeader={jest.fn()}
    />,
  );

  expect(compare[0]).toBeInTheDocument();
  fireEvent.click(compare[0]);

  expect(setVersion).toHaveBeenCalledTimes(6);
  expect(setVersion).toHaveBeenLastCalledWith("2", "3");
});

test("triggers header callbacks", () => {
  const onClick = jest.fn();
  const onMouseOver = jest.fn();
  const onMouseOut = jest.fn();

  render(
    <SourceVersionSelect
      versions={[
        { id: "1", name: "One" },
        { id: "2", name: "Two" },
      ]}
      selected={{ original: "2" }}
      expanded={false}
      tabIcon={"tabIcon" as any}
      setVersion={jest.fn()}
      onClickHeader={onClick}
      onMouseOverHeader={onMouseOver}
      onMouseOutHeader={onMouseOut}
    />,
  );

  const header = screen.getByTestId("tabs-1")?.parentElement as HTMLElement;
  expect(header).toBeDefined();
  expect(header).toBeInTheDocument();

  expect(onClick).not.toHaveBeenCalled();
  expect(onMouseOver).not.toHaveBeenCalled();
  expect(onMouseOut).not.toHaveBeenCalled();

  fireEvent.mouseOver(header);

  expect(onClick).not.toHaveBeenCalled();
  expect(onMouseOver).toHaveBeenCalledTimes(1);
  expect(onMouseOut).not.toHaveBeenCalled();

  fireEvent.click(header);

  expect(onClick).toHaveBeenCalledTimes(1);
  expect(onMouseOver).toHaveBeenCalledTimes(1);
  expect(onMouseOut).not.toHaveBeenCalled();

  fireEvent.mouseOut(header);
  expect(onClick).toHaveBeenCalledTimes(1);
  expect(onMouseOver).toHaveBeenCalledTimes(1);
  expect(onMouseOut).toHaveBeenCalledTimes(1);
});
