import React from "react";
import { render, screen } from "@testing-library/react";
import App from "./App";

describe("App", () => {
  test("renders correctly", () => {
    const { asFragment } = render(<App />);
    expect(asFragment()).toMatchSnapshot();
  });

  test("renders learn react link", () => {
    render(<App />);
    const linkElement = screen.getByText(/learn react/i);
    expect(linkElement).toBeInTheDocument();
  });
});
