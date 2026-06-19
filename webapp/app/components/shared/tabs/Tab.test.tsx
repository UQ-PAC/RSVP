import { fireEvent, render, screen } from "@testing-library/react";
import { Tab } from "./Tab";

test("renders", () => {
  const { asFragment, rerender } = render(
    <Tab select={jest.fn()}>
      <span>Tab Name</span>
    </Tab>,
  );
  expect(asFragment()).toMatchSnapshot();

  // Renders selected
  rerender(
    <Tab selected select={jest.fn()}>
      <span>Tab Name</span>
    </Tab>,
  );
  expect(asFragment()).toMatchSnapshot();

  // Renders dark theme
  rerender(
    <Tab select={jest.fn()} theme="dark">
      <span>Tab Name</span>
    </Tab>,
  );
  expect(asFragment()).toMatchSnapshot();

  // Renders without children
  rerender(<Tab select={jest.fn()} />);
  expect(asFragment()).toMatchSnapshot();
});

test("triggers selection", () => {
  const select = jest.fn();
  const clickParent = jest.fn();

  const { rerender } = render(
    <div onClick={clickParent}>
      <Tab select={select} />
    </div>,
  );

  expect(select).not.toHaveBeenCalled();

  const tab = screen.getByTestId("tab");
  expect(tab).toBeInTheDocument();

  fireEvent.click(tab);
  expect(select).toHaveBeenCalledTimes(1);
  expect(clickParent).not.toHaveBeenCalled();

  // Don't trigger selection if already selected
  rerender(
    <div onClick={clickParent}>
      <Tab select={select} selected />
    </div>,
  );
  expect(tab).toBeInTheDocument();

  fireEvent.click(tab);
  expect(select).toHaveBeenCalledTimes(1);
  expect(clickParent).not.toHaveBeenCalled();
});

test("prevents mouseOver propagation", () => {
  const mouseOverParent = jest.fn();

  // Tab deselected
  const { rerender } = render(
    <div onMouseOver={mouseOverParent}>
      <Tab select={jest.fn()} />
      <div data-testid="tab-sibling" />
    </div>,
  );

  const tab = screen.getByTestId("tab");
  const sibling = screen.getByTestId("tab-sibling");
  expect(tab).toBeInTheDocument();
  expect(sibling).toBeInTheDocument();

  fireEvent.mouseOver(tab);
  expect(mouseOverParent).not.toHaveBeenCalled();

  fireEvent.mouseOver(sibling);
  expect(mouseOverParent).toHaveBeenCalledTimes(1);

  // Tab selected
  mouseOverParent.mockClear();
  rerender(
    <div onMouseOver={mouseOverParent}>
      <Tab select={jest.fn()} selected />
      <div data-testid="tab-sibling" />
    </div>,
  );

  expect(tab).toBeInTheDocument();
  expect(sibling).toBeInTheDocument();

  fireEvent.mouseOver(tab);
  expect(mouseOverParent).not.toHaveBeenCalled();

  fireEvent.mouseOver(sibling);
  expect(mouseOverParent).toHaveBeenCalledTimes(1);
});
