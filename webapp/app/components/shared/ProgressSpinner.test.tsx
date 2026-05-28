import { render } from "@testing-library/react";
import { ProgressSpinner } from "./ProgressSpinner";

jest.mock("@fortawesome/react-fontawesome", () => ({
  FontAwesomeIcon: jest.fn(({ icon }) => (
    <div data-testid="font-awesome-icon" data-icon={icon} />
  )),
}));

jest.mock("@fortawesome/free-solid-svg-icons", () => ({
  faSpinner: "faSpinner",
}));

test("renders", () => {
  const { asFragment, rerender } = render(<ProgressSpinner />);
  expect(asFragment()).toMatchSnapshot();

  rerender(<ProgressSpinner text="Testing in progress..." />);
  expect(asFragment()).toMatchSnapshot();
});
