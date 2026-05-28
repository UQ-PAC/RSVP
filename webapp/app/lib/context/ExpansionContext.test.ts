import { ExpansionStatus, expansionReducer } from "./ExpansionContext";

test("handles invalid registration", () => {
  const initial = {
    drawer: {
      expansions: {},
    },
  };

  // focus state not specified, return initial context
  expect(
    expansionReducer(initial, {
      type: "register",
      group: "drawer",
      id: "foo",
      conflict: "bar",
    }),
  ).toBe(initial);

  // group not specified, return initial context
  expect(
    expansionReducer(initial, {
      type: "register",
      group: "drawer",
      id: "foo",
      status: ExpansionStatus.Collapsed,
    }),
  ).toBe(initial);
});

test("handles valid registration", () => {
  const initial = {
    drawer: {
      expansions: {},
    },
  };

  // Register new file group with collapsed file
  let updated = expansionReducer(initial, {
    type: "register",
    group: "drawer",
    id: "foo",
    conflict: "foo",
    status: ExpansionStatus.Collapsed,
  });

  expect(updated).toEqual({
    drawer: {
      expansions: {
        foo: ExpansionStatus.Collapsed,
      },
      conflicts: {
        foo: ["foo"],
      },
    },
  });

  // Add expanded file to existing group
  updated = expansionReducer(updated, {
    type: "register",
    group: "drawer",
    id: "bar",
    conflict: "foo",
    status: ExpansionStatus.Expanded,
  });

  expect(updated).toEqual({
    drawer: {
      expansions: {
        foo: ExpansionStatus.Collapsed,
        bar: ExpansionStatus.Expanded,
      },
      conflicts: {
        foo: ["foo", "bar"],
      },
    },
  });

  // Add expanded file to existing group, collapse other files
  updated = expansionReducer(updated, {
    type: "register",
    group: "drawer",
    id: "baz",
    conflict: "foo",
    status: ExpansionStatus.Expanded,
  });

  expect(updated).toEqual({
    drawer: {
      expansions: {
        foo: ExpansionStatus.Collapsed,
        bar: ExpansionStatus.Collapsed,
        baz: ExpansionStatus.Expanded,
      },
      conflicts: {
        foo: ["foo", "bar", "baz"],
      },
    },
  });

  // Add collapsed file to existing group, does not affect other files
  updated = expansionReducer(updated, {
    type: "register",
    group: "drawer",
    id: "fez",
    conflict: "foo",
    status: ExpansionStatus.Collapsed,
  });
  expect(updated).toEqual({
    drawer: {
      expansions: {
        foo: ExpansionStatus.Collapsed,
        bar: ExpansionStatus.Collapsed,
        baz: ExpansionStatus.Expanded,
        fez: ExpansionStatus.Collapsed,
      },
      conflicts: {
        foo: ["foo", "bar", "baz", "fez"],
      },
    },
  });
});

test("handles invalid deregistration", () => {
  const initial = {
    drawer: {
      expansions: {
        foo: ExpansionStatus.Collapsed,
        bar: ExpansionStatus.Expanded,
        baz: ExpansionStatus.Collapsed,
      },
    },
  };

  // deregister group with no conflicts
  expect(
    expansionReducer(initial, {
      type: "deregister",
      group: "drawer",
      id: "foo",
    }),
  ).toBe(initial);
});

test("handles deregistration", () => {
  const initial = {
    drawer: {
      expansions: {
        foo: ExpansionStatus.Collapsed,
        bar: ExpansionStatus.Expanded,
        baz: ExpansionStatus.Collapsed,
      },
      conflicts: {
        foo: ["foo", "bar", "baz"],
      },
    },
  };

  // deregister group
  expect(
    expansionReducer(initial, {
      type: "deregister",
      group: "drawer",
      id: "foo",
    }),
  ).toEqual({
    drawer: {
      expansions: {},
      conflicts: {},
    },
  });

  // deregister single file
  expect(
    expansionReducer(initial, {
      type: "deregister",
      group: "drawer",
      id: "baz",
    }),
  ).toEqual({
    drawer: {
      expansions: {
        foo: ExpansionStatus.Collapsed,
        bar: ExpansionStatus.Expanded,
      },
      conflicts: {
        foo: ["foo", "bar"],
      },
    },
  });

  // deregister single expanded file, expand remaining file
  expect(
    expansionReducer(initial, {
      type: "deregister",
      group: "drawer",
      id: "bar",
    }),
  ).toEqual({
    drawer: {
      expansions: {
        foo: ExpansionStatus.Collapsed,
        baz: ExpansionStatus.Expanded,
      },
      conflicts: {
        foo: ["foo", "baz"],
      },
    },
  });

  // attempt to deregister file that doesn't exist
  expect(
    expansionReducer(initial, {
      type: "deregister",
      group: "drawer",
      id: "fake",
    }),
  ).toEqual(initial);
});

test("handles deregistration with kind", () => {
  const initial = {
    drawer: {
      expansions: {
        one: ExpansionStatus.Collapsed,
        "a/two": ExpansionStatus.Expanded,
        three: ExpansionStatus.Collapsed,
        "a/four": ExpansionStatus.Collapsed,
        five: ExpansionStatus.Collapsed,
      },
      conflicts: {
        one: ["one", "a/two", "three", "a/four", "five"],
      },
    },
  };

  const updated = expansionReducer(initial, {
    type: "deregister",
    group: "drawer",
    id: "two",
    kind: "a",
  });

  // deregister expanded file, expand file of correct kind
  expect(updated).toEqual({
    drawer: {
      expansions: {
        one: ExpansionStatus.Collapsed,
        three: ExpansionStatus.Collapsed,
        "a/four": ExpansionStatus.Expanded,
        five: ExpansionStatus.Collapsed,
      },
      conflicts: {
        one: ["one", "three", "a/four", "five"],
      },
    },
  });

  // deregister only file of kind, expand original file
  expect(
    expansionReducer(updated, {
      type: "deregister",
      group: "drawer",
      id: "four",
      kind: "a",
    }),
  ).toEqual({
    drawer: {
      expansions: {
        one: ExpansionStatus.Expanded,
        three: ExpansionStatus.Collapsed,
        five: ExpansionStatus.Collapsed,
      },
      conflicts: {
        one: ["one", "three", "five"],
      },
    },
  });
});

test("handles invalid toggle", () => {
  const initial = {};

  expect(
    expansionReducer(initial, {
      type: "toggle",
      group: "drawer",
      id: "foo",
    }),
  ).toBe(initial);
});

test("toggles standalone item", () => {
  const initial = {
    drawer: {
      expansions: {
        foo: ExpansionStatus.Collapsed,
        bar: ExpansionStatus.Expanded,
        baz: ExpansionStatus.Collapsed,
      },
    },
  };

  // trigger expansion
  expect(
    expansionReducer(initial, {
      type: "toggle",
      group: "drawer",
      id: "foo",
      status: ExpansionStatus.Expanded,
    }),
  ).toEqual({
    drawer: {
      expansions: {
        foo: ExpansionStatus.Expanded,
        bar: ExpansionStatus.Expanded,
        baz: ExpansionStatus.Collapsed,
      },
    },
  });

  // trigger collapse
  expect(
    expansionReducer(initial, {
      type: "toggle",
      group: "drawer",
      id: "bar",
      status: ExpansionStatus.Collapsed,
    }),
  ).toEqual({
    drawer: {
      expansions: {
        foo: ExpansionStatus.Collapsed,
        bar: ExpansionStatus.Collapsed,
        baz: ExpansionStatus.Collapsed,
      },
    },
  });

  // unnecessary trigger, no modifications to state
  expect(
    expansionReducer(initial, {
      type: "toggle",
      group: "drawer",
      id: "bar",
      status: ExpansionStatus.Expanded,
    }),
  ).toBe(initial);
});

test("toggles conflicting item", () => {
  const initial = {
    drawer: {
      expansions: {
        foo: ExpansionStatus.Collapsed,
        bar: ExpansionStatus.Expanded,
        baz: ExpansionStatus.Collapsed,
      },
      conflicts: {
        foo: ["foo", "bar", "baz"],
      },
    },
  };

  // Expansion should collapse all conflicting targets
  expect(
    expansionReducer(initial, {
      type: "toggle",
      group: "drawer",
      id: "baz",
      status: ExpansionStatus.Expanded,
    }),
  ).toEqual({
    drawer: {
      expansions: {
        foo: ExpansionStatus.Collapsed,
        bar: ExpansionStatus.Collapsed,
        baz: ExpansionStatus.Expanded,
      },
      conflicts: {
        foo: ["foo", "bar", "baz"],
      },
    },
  });

  // Collapse does not affect conflicting targets
  expect(
    expansionReducer(initial, {
      type: "toggle",
      group: "drawer",
      id: "bar",
      status: ExpansionStatus.Collapsed,
    }),
  ).toEqual({
    drawer: {
      expansions: {
        foo: ExpansionStatus.Collapsed,
        bar: ExpansionStatus.Collapsed,
        baz: ExpansionStatus.Collapsed,
      },
      conflicts: {
        foo: ["foo", "bar", "baz"],
      },
    },
  });
});
