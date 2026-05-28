import { act, fireEvent, render, screen } from "@testing-library/react";
import { FileInput } from "./FileInput";

var files = ["file-one.txt", "file-two.txt"];

jest.mock("./HiddenFileInput", () => ({
  HiddenFileInput: jest.fn(({ ref, accept, handleFileInput }) => (
    <div
      ref={ref}
      data-testid="hidden-file-input"
      data-accept={accept}
      onClick={() => handleFileInput({ target: { files } })}
    />
  )),
}));

test("renders", () => {
  const { asFragment } = render(<FileInput addFiles={jest.fn()} />);
  expect(asFragment()).toMatchSnapshot();
});

test("handles drag and drop", () => {
  const addFiles = jest.fn();
  const { asFragment, rerender } = render(<FileInput addFiles={addFiles} />);

  expect(asFragment()).toMatchSnapshot();
  expect(addFiles).not.toHaveBeenCalled();

  const dropZone = screen.getByTestId("drop-zone");
  expect(dropZone).toBeInTheDocument();

  const clearData = jest.fn();
  const setData = jest.fn();

  // Drag over drop zone
  act(() => {
    fireEvent.dragStart(dropZone, { dataTransfer: { clearData, setData } });
    fireEvent.dragEnter(dropZone, { dataTransfer: { items: files } });
  });

  rerender(<FileInput addFiles={addFiles} />);
  expect(asFragment()).toMatchSnapshot();

  expect(clearData).toHaveBeenCalledTimes(1);
  expect(setData).toHaveBeenCalledTimes(1);
  expect(addFiles).not.toHaveBeenCalled();

  // Leave drop zone
  act(() => {
    fireEvent.dragLeave(dropZone);
  });

  rerender(<FileInput addFiles={addFiles} />);
  expect(asFragment()).toMatchSnapshot();

  expect(addFiles).not.toHaveBeenCalled();

  // Drop files in drop zone
  act(() => {
    fireEvent.dragEnter(dropZone, { dataTransfer: { items: files } });
    fireEvent.drop(dropZone, { dataTransfer: { files } });
  });

  rerender(<FileInput addFiles={addFiles} />);
  expect(asFragment()).toMatchSnapshot();

  expect(addFiles).toHaveBeenCalledTimes(1);
  expect(addFiles).toHaveBeenCalledWith(files);
});

test("handles click to browse", () => {
  const addFiles = jest.fn();
  render(<FileInput addFiles={addFiles} />);

  const dropZone = screen.getByTestId("drop-zone");
  expect(dropZone).toBeInTheDocument();

  expect(addFiles).not.toHaveBeenCalled();

  fireEvent.click(dropZone);

  expect(addFiles).toHaveBeenCalledTimes(1);
  expect(addFiles).toHaveBeenCalledWith(files);
});
