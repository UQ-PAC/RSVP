import { act, fireEvent, render, screen } from "@testing-library/react";
import { Tabs } from "./Tabs";

var selection: (() => void)[] = [];

jest.mock("./Tab", () => ({
  ...jest.requireActual("./Tab"),
  Tab: jest.fn(({ ref, selected, select, theme, children }) => {
    selection.push(select);
    return (
      <div
        ref={ref}
        data-testid="tab"
        data-selected={selected}
        data-theme={theme}
      >
        {children}
      </div>
    );
  }),
}));

beforeEach(() => {
  selection = [];
});

test("renders tabs", () => {
  const { asFragment, rerender } = render(
    <Tabs
      options={3}
      onSelect={jest.fn()}
      tabContent={(option: number) => <span>Tab {option}</span>}
    />,
  );
  expect(asFragment()).toMatchSnapshot();

  // Renders theme
  rerender(
    <Tabs
      options={3}
      onSelect={jest.fn()}
      tabContent={(option: number) => <span>Tab {option}</span>}
      tabTheme={(option: number) => (option % 2 ? "dark" : "light")}
    />,
  );
  expect(asFragment()).toMatchSnapshot();

  // Renders override option
  rerender(
    <Tabs
      options={3}
      overrideOption={1}
      onSelect={jest.fn()}
      tabContent={(option: number) => <span>Tab {option}</span>}
    />,
  );
  expect(asFragment()).toMatchSnapshot();
});

test("renders empty", () => {
  const { asFragment } = render(
    <Tabs options={0} onSelect={jest.fn()} tabContent={jest.fn()} />,
  );
  expect(asFragment()).toMatchSnapshot();
});

test("renders default option", () => {
  const { asFragment, rerender } = render(
    <Tabs
      options={3}
      defaultOption={2}
      onSelect={jest.fn()}
      tabContent={(option: number) => <span>Tab {option}</span>}
    />,
  );
  expect(asFragment()).toMatchSnapshot();

  // Renders override option
  rerender(
    <Tabs
      options={3}
      defaultOption={2}
      overrideOption={1}
      onSelect={jest.fn()}
      tabContent={(option: number) => <span>Tab {option}</span>}
    />,
  );
  expect(asFragment()).toMatchSnapshot();
});

test("handles selection", () => {
  const select = jest.fn();

  const { asFragment, rerender } = render(
    <Tabs
      options={3}
      onSelect={select}
      tabContent={(option: number) => <span>Tab {option}</span>}
    />,
  );
  expect(asFragment()).toMatchSnapshot();

  expect(select).not.toHaveBeenCalled();
  expect(selection).toHaveLength(3);

  act(() => {
    selection[1]();
    rerender(
      <Tabs
        options={3}
        onSelect={select}
        tabContent={(option: number) => <span>Tab {option}</span>}
      />,
    );
  });

  expect(asFragment()).toMatchSnapshot();

  expect(select).toHaveBeenCalledTimes(1);
  expect(select).toHaveBeenCalledWith(1);

  act(() => {
    selection[2]();
    rerender(
      <Tabs
        options={3}
        onSelect={select}
        tabContent={(option: number) => <span>Tab {option}</span>}
      />,
    );
  });

  expect(asFragment()).toMatchSnapshot();

  expect(select).toHaveBeenCalledTimes(2);
  expect(select).toHaveBeenNthCalledWith(2, 2);
});

test("handles wheel events", () => {
  const parentWheel = jest.fn();

  render(
    <div data-testid="tab-container" onWheel={parentWheel}>
      <Tabs
        options={3}
        onSelect={jest.fn()}
        tabContent={(option: number) => <span>Tab {option}</span>}
      />
    </div>,
  );

  const tabs = screen.getByTestId("tabs");
  expect(tabs).toBeInTheDocument();

  // Mock scrollTo function
  const mockScrollTo = jest.fn();
  tabs.scrollTo = mockScrollTo;
  tabs.scrollLeft = 100;

  fireEvent.wheel(tabs, { deltaY: 50 });
  expect(mockScrollTo).toHaveBeenCalled();
  expect(mockScrollTo).toHaveBeenCalledWith({ left: 250, behavior: "smooth" });
  expect(parentWheel).not.toHaveBeenCalled();
});

test("scrolls tabs on selection", async () => {
  const tabContent = (option: number) => <span>Tab {option}</span>;
  const { rerender } = render(
    <Tabs
      options={10}
      overrideOption={0}
      onSelect={jest.fn()}
      tabContent={tabContent}
    />,
  );

  const tabs = screen.getByTestId("tabs");
  expect(tabs).toBeInTheDocument();

  const fourthTab = screen.getByText("Tab 3")?.parentElement as HTMLElement;
  expect(fourthTab).toBeTruthy();
  expect(fourthTab).toBeInTheDocument();

  const seventhTab = screen.getByText("Tab 6")?.parentElement as HTMLElement;
  expect(seventhTab).toBeTruthy();
  expect(seventhTab).toBeInTheDocument();

  const scrollFourthTabIntoView = jest.fn();
  const scrollSeventhTabIntoView = jest.fn();
  fourthTab.scrollIntoView = scrollFourthTabIntoView;
  seventhTab.scrollIntoView = scrollSeventhTabIntoView;

  // Fourth tab is hidden on left, seventh tab is hidden on right
  jest.spyOn(tabs, "offsetLeft", "get").mockImplementation(() => 50);
  jest.spyOn(tabs, "offsetWidth", "get").mockImplementation(() => 300);
  tabs.scrollLeft = 500;

  jest.spyOn(fourthTab, "offsetLeft", "get").mockImplementation(() => 350);
  jest.spyOn(fourthTab, "offsetWidth", "get").mockImplementation(() => 100);

  jest.spyOn(seventhTab, "offsetLeft", "get").mockImplementation(() => 950);
  jest.spyOn(seventhTab, "offsetWidth", "get").mockImplementation(() => 100);

  // Select fourth tab (hidden to left)
  act(() => {
    rerender(
      <Tabs
        options={10}
        overrideOption={3}
        onSelect={jest.fn()}
        tabContent={tabContent}
      />,
    );
  });

  expect(scrollFourthTabIntoView).toHaveBeenCalledTimes(1);
  expect(scrollSeventhTabIntoView).not.toHaveBeenCalled();

  // Select seventh tab (hidden to right)
  act(() => {
    rerender(
      <Tabs
        options={10}
        overrideOption={6}
        onSelect={jest.fn()}
        tabContent={tabContent}
      />,
    );
  });

  expect(scrollFourthTabIntoView).toHaveBeenCalledTimes(1);
  expect(scrollSeventhTabIntoView).toHaveBeenCalledTimes(1);
});
