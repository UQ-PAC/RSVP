import { fireEvent, render, screen } from "@testing-library/react";
import { ExpansionStatus } from "../../lib/context/ExpansionContext";
import { ToggleAll } from "./ToggleAll";

test("renders", () => {
  const { asFragment } = render(<ToggleAll name="test" toggle={jest.fn()} />);
  expect(asFragment()).toMatchSnapshot();
});

test("triggers toggle", () => {
  const toggle = jest.fn();
  render(<ToggleAll name="test" toggle={toggle} />);
  const expand = screen.getByTestId("expand-all");
  expect(expand).toBeInTheDocument();

  fireEvent.click(expand);
  expect(toggle).toHaveBeenCalledTimes(1);
  expect(toggle).toHaveBeenNthCalledWith(1, ExpansionStatus.Expanded);

  const collapse = screen.getByTestId("collapse-all");
  expect(collapse).toBeInTheDocument();

  fireEvent.click(collapse);
  expect(toggle).toHaveBeenCalledTimes(2);
  expect(toggle).toHaveBeenNthCalledWith(2, ExpansionStatus.Collapsed);
});
