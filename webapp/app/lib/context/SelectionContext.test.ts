import { selectionReducer } from "./SelectionContext";

test("handles selection action", () => {
  const initial = {
    selected: "12345",
    hovered: "67890",
  };

  expect(
    selectionReducer(initial, {
      selected: "12345",
    }),
  ).toBe(initial);

  expect(
    selectionReducer(initial, {
      selected: "67890",
    }),
  ).toEqual({
    selected: "67890",
    hovered: "67890",
  });

  expect(
    selectionReducer(initial, {
      group: "group",
      scroll: "none",
    }),
  ).toEqual({
    selected: "12345",
    hovered: "67890",
    group: "group",
    scroll: "none",
  });
});
