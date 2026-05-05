import { fireEvent, render, screen } from "@testing-library/react";
import { CreateContextButton } from "./CreateContextButton";

jest.mock("@fortawesome/react-fontawesome", () => ({
  FontAwesomeIcon: jest.fn(({ icon }) => (
    <div data-testid="font-awesome-icon" data-icon={icon} />
  )),
}));

jest.mock("@fortawesome/free-solid-svg-icons", () => ({
  faPlus: "faPlus",
}));

test("renders", () => {
  const { asFragment } = render(<CreateContextButton onclick={jest.fn()} />);
  expect(asFragment()).toMatchSnapshot();
});

test("clicks", () => {
  const onclick = jest.fn();
  render(<CreateContextButton onclick={onclick} />);
  const button = screen.getByTestId("create-policy-context-button");
  expect(button).toBeInTheDocument();

  fireEvent.click(button);
  expect(onclick).toHaveBeenCalledTimes(1);

  fireEvent.click(button);
  expect(onclick).toHaveBeenCalledTimes(2);
});
