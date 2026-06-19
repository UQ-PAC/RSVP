import { act, render, screen, waitFor } from "@testing-library/react";
import { CodeLine } from "./CodeLine";

// Mock selection context
var noSelection = {
  scroll: "none",
};

var selectionContext;

// Mock syntax highlight function
var highlight = jest.fn().mockReturnValue("Highlighted code!!");

jest.mock("../../lib/context/SelectionContext", () => ({
  useSelection: jest.fn(() => selectionContext),
}));

jest.mock("../../lib/sources/util", () => ({
  getHighlightFunction: jest.fn((syntax) => () => highlight(syntax)),
}));

beforeEach(() => {
  selectionContext = noSelection;
  highlight.mockClear();
});

test("renders", () => {
  const { asFragment, rerender } = render(
    <CodeLine
      file={Promise.resolve("123")}
      n={10}
      syntax={"text"}
      temporaryHighlight={jest.fn()}
    >
      A line of code!
    </CodeLine>,
  );
  expect(asFragment()).toMatchSnapshot();

  expect(highlight).toHaveBeenCalledTimes(1);
  expect(highlight).toHaveBeenCalledWith("text");

  // Highlight line
  rerender(
    <CodeLine
      file={Promise.resolve("123")}
      n={10}
      syntax={"text"}
      temporaryHighlight={jest.fn().mockReturnValue(true)}
    >
      A line of code!
    </CodeLine>,
  );
  expect(asFragment()).toMatchSnapshot();
});

test("triggers scroll on selection", async () => {
  const { rerender } = render(
    <CodeLine
      file={Promise.resolve("123")}
      n={10}
      syntax={"text"}
      temporaryHighlight={jest.fn()}
    >
      A line of code!
    </CodeLine>,
  );

  const elem = screen.getByTestId("code-line");
  expect(elem).toBeInTheDocument();
  elem.scrollIntoView = jest.fn();

  // Scroll to selected line
  act(() => {
    selectionContext = {
      scroll: "source",
      loc: "123:10",
    };

    rerender(
      <CodeLine
        file={Promise.resolve("123")}
        n={10}
        syntax={"text"}
        temporaryHighlight={jest.fn()}
      >
        A line of code!
      </CodeLine>,
    );
  });

  await waitFor(() => {
    expect(elem.scrollIntoView).toHaveBeenCalledTimes(1);
  });

  // Don't scroll if selection unchanged
  act(() => {
    selectionContext = {
      scroll: "source",
      loc: "123:10",
    };

    rerender(
      <CodeLine
        file={Promise.resolve("123")}
        n={10}
        syntax={"text"}
        temporaryHighlight={jest.fn()}
      >
        A line of code!
      </CodeLine>,
    );
  });

  await waitFor(() => {
    expect(elem.scrollIntoView).toHaveBeenCalledTimes(1);
  });

  // Don't scroll to unselected line
  act(() => {
    selectionContext = {
      scroll: "source",
      loc: "123:11",
    };
    rerender(
      <CodeLine
        file={Promise.resolve("123")}
        n={10}
        syntax={"text"}
        temporaryHighlight={jest.fn()}
      >
        A line of code!
      </CodeLine>,
    );
  });

  await waitFor(() => {
    expect(elem.scrollIntoView).toHaveBeenCalledTimes(1);
  });
});
