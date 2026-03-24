import React from "react";
import { render, screen } from "@testing-library/react";
import Home from "./page";

// Mock CSS so that Jest doesn't try to parse it as JS
jest.mock("filepond/dist/filepond.min.css", () => "");
jest.mock("next/font/google", () => ({
  Roboto_Mono: jest.fn().mockReturnValue({ className: "" }),
  Lexend_Deca: jest.fn().mockReturnValue({ className: "" }),
  Lexend_Giga: jest.fn().mockReturnValue({ className: "" }),
}));

describe("App", () => {
  // test("renders correctly", () => {
  //   const { asFragment } = render(<App />);
  //   expect(asFragment()).toMatchSnapshot();
  // });

  test("renders learn react link", () => {
    render(<Home />);
    const title = screen.getByText(/Policy Verification/i);
    expect(title).toBeInTheDocument();
  });
});
