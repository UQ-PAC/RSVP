/* eslint-disable @typescript-eslint/no-explicit-any */
import { fireEvent, render, screen } from "@testing-library/react";
import { UploadedFile } from "./UploadedFile";

jest.mock("@fortawesome/react-fontawesome", () => ({
  FontAwesomeIcon: jest.fn(({ icon, onClick }) => (
    <div
      data-testid={`font-awesome-icon-${icon}`}
      data-icon={icon}
      onClick={onClick}
    />
  )),
}));

jest.mock("@fortawesome/free-solid-svg-icons", () => ({
  faCodeCompare: "faCodeCompare",
  faXmark: "faXmark",
}));

const file = { filename: "test-file" } as any;

test("renders", () => {
  const { asFragment, rerender } = render(
    <UploadedFile file={file} remove={jest.fn()} />,
  );
  expect(asFragment()).toMatchSnapshot();

  rerender(
    <UploadedFile file={file} addChild={jest.fn()} remove={jest.fn()} />,
  );
  expect(asFragment()).toMatchSnapshot();

  rerender(
    <UploadedFile file={file} addChild={jest.fn()} remove={jest.fn()}>
      <div key="1" data-testid="nested-file" />
    </UploadedFile>,
  );
  expect(asFragment()).toMatchSnapshot();

  rerender(
    <UploadedFile file={file} addChild={jest.fn()} remove={jest.fn()}>
      <div key="1" data-testid="nested-file-one" />
      <div key="2" data-testid="nested-file-two" />
    </UploadedFile>,
  );
  expect(asFragment()).toMatchSnapshot();
});

test("adds child", () => {
  const addChild = jest.fn();
  render(<UploadedFile file={file} addChild={addChild} remove={jest.fn()} />);
  const button = screen.getByTestId("font-awesome-icon-faCodeCompare");
  expect(button).toBeInTheDocument();

  fireEvent.click(button);
  expect(addChild).toHaveBeenCalledTimes(1);
  expect(addChild).toHaveBeenCalledWith(file);

  fireEvent.click(button);
  expect(addChild).toHaveBeenCalledTimes(2);
});

test("removes child", () => {
  const remove = jest.fn();
  render(<UploadedFile file={file} remove={remove} />);
  const button = screen.getByTestId("font-awesome-icon-faXmark");
  expect(button).toBeInTheDocument();

  fireEvent.click(button);
  expect(remove).toHaveBeenCalledTimes(1);
  expect(remove).toHaveBeenCalledWith(file);

  fireEvent.click(button);
  expect(remove).toHaveBeenCalledTimes(2);
});
