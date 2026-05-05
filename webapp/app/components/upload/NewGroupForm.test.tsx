import { fireEvent, render, screen } from "@testing-library/react";
import { NewGroupForm } from "./NewGroupForm";

jest.mock("@fortawesome/react-fontawesome", () => ({
  FontAwesomeIcon: jest.fn(({ icon }) => (
    <div data-testid="font-awesome-icon" data-icon={icon} />
  )),
}));

jest.mock("@fortawesome/free-solid-svg-icons", () => ({
  faCirclePlus: "faCirclePlus",
  faCircleXmark: "faCircleXmark",
}));

test("renders", () => {
  const { asFragment } = render(
    <NewGroupForm
      index={0}
      create={jest.fn()}
      cancel={jest.fn()}
      existing={[]}
    />,
  );
  expect(asFragment()).toMatchSnapshot();
});

test("creates group", () => {
  const create = jest.fn();
  const { asFragment } = render(
    <NewGroupForm index={7} create={create} cancel={jest.fn()} existing={[]} />,
  );
  expect(asFragment()).toMatchSnapshot();

  const input = screen.getByTestId("create-analysis-group-text-input");
  const button = screen.getByTestId("create-analysis-group-button");
  expect(input).toBeInTheDocument();
  expect(button).toBeInTheDocument();
  expect(button).toBeEnabled();

  // Create with placeholder name
  fireEvent.click(button);
  expect(create).toHaveBeenCalledTimes(1);
  expect(create).toHaveBeenCalledWith("Policy Set 7");

  // Create with custom name
  fireEvent.change(input, { target: { value: "Test Group" } });
  fireEvent.click(button);
  expect(create).toHaveBeenCalledTimes(2);
  expect(create).toHaveBeenNthCalledWith(2, "Test Group");

  // Delete name and create with placeholder
  fireEvent.change(input, { target: { value: "" } });
  fireEvent.click(button);
  expect(create).toHaveBeenCalledTimes(3);
  expect(create).toHaveBeenNthCalledWith(3, "Policy Set 7");
});

test("cancels group creation", () => {
  const cancel = jest.fn();
  const { asFragment } = render(
    <NewGroupForm
      index={100}
      create={jest.fn()}
      cancel={cancel}
      existing={[]}
    />,
  );
  expect(asFragment()).toMatchSnapshot();

  const button = screen.getByTestId("cancel-create-analysis-group-button");
  expect(button).toBeInTheDocument();

  fireEvent.click(button);
  expect(cancel).toHaveBeenCalledTimes(1);

  fireEvent.click(button);
  expect(cancel).toHaveBeenCalledTimes(2);
});

test("validates group name", () => {
  const create = jest.fn();
  const cancel = jest.fn();
  const existing = ["Existing Group", "the best group", "Policy Set 35"];
  const { asFragment, rerender } = render(
    <NewGroupForm
      index={35}
      create={create}
      cancel={cancel}
      existing={existing}
    />,
  );
  expect(asFragment()).toMatchSnapshot();

  const input = screen.getByTestId("create-analysis-group-text-input");
  const button = screen.getByTestId("create-analysis-group-button");
  expect(input).toBeInTheDocument();
  expect(button).toBeInTheDocument();

  // Policy Group 35 exists already
  expect(button).toBeDisabled();

  fireEvent.click(button);
  expect(create).toHaveBeenCalledTimes(0);

  // Change to non-existing group
  fireEvent.change(input, { target: { value: "New Group" } });

  rerender(
    <NewGroupForm
      index={35}
      create={create}
      cancel={cancel}
      existing={existing}
    />,
  );
  expect(asFragment()).toMatchSnapshot();

  expect(button).toBeEnabled();

  fireEvent.click(button);
  expect(create).toHaveBeenCalledTimes(1);
  expect(create).toHaveBeenCalledWith("New Group");

  // Existence check should ignore case
  fireEvent.change(input, { target: { value: "The Best Group" } });

  rerender(
    <NewGroupForm
      index={35}
      create={create}
      cancel={cancel}
      existing={existing}
    />,
  );
  expect(asFragment()).toMatchSnapshot();

  expect(button).toBeDisabled();

  fireEvent.click(button);
  expect(create).toHaveBeenCalledTimes(1);

  fireEvent.change(input, { target: { value: "existing group" } });

  rerender(
    <NewGroupForm
      index={35}
      create={create}
      cancel={cancel}
      existing={existing}
    />,
  );

  expect(button).toBeDisabled();

  fireEvent.click(button);
  expect(create).toHaveBeenCalledTimes(1);
});
