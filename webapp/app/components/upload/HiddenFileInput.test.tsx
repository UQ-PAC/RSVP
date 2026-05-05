import { fireEvent, render, screen } from "@testing-library/react";
import { HiddenFileInput } from "./HiddenFileInput";

test("renders", () => {
  const { asFragment } = render(
    <HiddenFileInput
      accept=".txt"
      ref={jest.fn()}
      handleFileInput={jest.fn()}
    />,
  );
  expect(asFragment()).toMatchSnapshot();
});

test("handles change", () => {
  const onchange = jest.fn();

  render(
    <HiddenFileInput
      accept=".txt"
      ref={jest.fn()}
      handleFileInput={onchange}
    />,
  );

  const input = screen.getByTestId("hidden-file-input");
  expect(input).toBeInTheDocument();

  fireEvent.change(input);
  expect(onchange).toHaveBeenCalledTimes(1);
});
