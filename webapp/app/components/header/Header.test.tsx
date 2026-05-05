import { render } from "@testing-library/react";

import { Header } from "./Header";

jest.mock("next/font/google", () => ({
  Lexend_Giga: jest.fn().mockReturnValue({ className: "lexend-giga" }),
}));

jest.mock("./VerifyButton", () => ({
  VerifyButton: jest.fn(() => <div data-testid="verify-button" />),
}));

test("renders with subheading", () => {
  const { asFragment } = render(
    <Header heading="Test Heading" subheading="Test Subheading" />,
  );
  expect(asFragment()).toMatchSnapshot();
});

test("renders without subheading", () => {
  const { asFragment } = render(<Header heading="Test Heading" />);
  expect(asFragment()).toMatchSnapshot();
});
