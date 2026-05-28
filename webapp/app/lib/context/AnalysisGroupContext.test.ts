import { AnalysisGroup, Report } from "../types";
import { reducer } from "./AnalysisGroupContext";

let consoleErr;

beforeAll(() => {
  consoleErr = global.console.error;
  global.console.error = jest.fn();
});

beforeEach(() => {
  (global.console.error as any)?.mockClear?.();
});

afterAll(() => {
  global.console.error = consoleErr;
});

test("valid add", async () => {
  let context: AnalysisGroup = {
    name: "group",
    files: [],
    diffs: {},
    verifyPending: false,
    verifyComplete: false,
  };

  const prev = context;

  // Add file
  context = reducer(context, {
    type: "add",
    file: {
      file: {
        name: "file.txt",
      },
      filetype: "cedar",
      resolved: Promise.resolve({
        serverId: "12345",
      }),
    } as any,
  });

  // Returns new object
  expect(context).not.toBe(prev);

  expect(context.files.length).toBe(1);
  expect(context.files[0].versions).toHaveLength(0);

  const file = context.files[0].original;
  expect(file.filename).toEqual("file.txt");
  expect(file.file.name).toEqual("file.txt");
  expect(file.filetype).toEqual("cedar");
  expect(await file.resolved).toEqual({ serverId: "12345" });

  // Files are sorted alphabetically
  context = reducer(context, {
    type: "add",
    file: {
      file: {
        name: "another-file.txt",
      },
      filetype: "cedar",
      resolved: Promise.resolve({
        serverId: "67890",
      }),
    } as any,
  });

  expect(context.files.length).toBe(2);
  expect(context.files[0].versions).toHaveLength(0);
  expect(context.files[1].versions).toHaveLength(0);

  const first = context.files[0].original;
  const second = context.files[1].original;

  expect(first.filename).toEqual("another-file.txt");
  expect(first.file.name).toEqual("another-file.txt");
  expect(first.filetype).toEqual("cedar");
  expect(await first.resolved).toEqual({ serverId: "67890" });

  expect(second.filename).toEqual("file.txt");

  // Add file with duplicate name
  context = reducer(context, {
    type: "add",
    file: {
      file: {
        name: "file.txt",
      },
      filetype: "cedar",
      resolved: Promise.resolve({
        serverId: "54321",
      }),
    } as any,
  });

  expect(context.files.length).toBe(3);
  expect(context.files[2].versions).toHaveLength(0);

  expect(context.files[0].original.filename).toEqual("another-file.txt");
  expect(context.files[1].original.filename).toEqual("file.txt");

  const duplicate = context.files[2].original;
  expect(duplicate.filename).toEqual("file.txt (1)");
  expect(duplicate.file.name).toEqual("file.txt");
  expect(duplicate.filetype).toEqual("cedar");
  expect(await duplicate.resolved).toEqual({ serverId: "54321" });

  // Add another file with duplicate name
  context = reducer(context, {
    type: "add",
    file: {
      file: {
        name: "file.txt",
      },
      filetype: "cedar",
      resolved: Promise.resolve({
        serverId: "00000",
      }),
    } as any,
  });

  const triplicate = context.files[3].original;
  expect(triplicate.filename).toEqual("file.txt (2)");
  expect(triplicate.file.name).toEqual("file.txt");
  expect(await triplicate.resolved).toEqual({ serverId: "00000" });

  // Add version
  context = reducer(context, {
    type: "add",
    file: {
      file: {
        name: "file.txt",
      },
      filetype: "cedar",
      resolved: Promise.resolve({
        serverId: "09876",
      }),
    } as any,
    original: context.files[1].original,
  });

  expect(context.files.length).toBe(4);
  expect(context.files[1].versions).toHaveLength(1);
  expect(context.files[1].original.filename).toEqual("file.txt");

  const version = context.files[1].versions[0];
  expect(version.filename).toEqual("file.txt");
  expect(version.file.name).toEqual("file.txt");
  expect(version.filetype).toEqual("cedar");
  expect(await version.resolved).toEqual({ serverId: "09876" });
});

test("invalid add", () => {
  // No file
  const context: AnalysisGroup = {
    name: "group",
    files: [],
    diffs: {},
    verifyPending: false,
    verifyComplete: false,
  };

  // Add file
  expect(
    reducer(context, {
      type: "add",
    }),
  ).toBe(context);

  expect(global.console.error).toHaveBeenLastCalledWith(
    'Invalid action: {"type":"add"}',
  );

  // Missing original file
  expect(
    reducer(context, {
      type: "add",
      file: {
        file: {
          name: "file.txt",
        },
        filetype: "cedar",
        resolved: Promise.resolve({
          serverId: "09876",
        }),
      } as any,
      original: {
        filetype: "cedar",
      } as any,
    }),
  ).toBe(context);

  expect(global.console.error).toHaveBeenLastCalledWith(
    "Adding version to nonexistent file: file.txt",
  );

  // Incompatible version filetype
  expect(
    reducer(context, {
      type: "add",
      file: {
        file: {
          name: "file.txt",
        },
        filetype: "cedar",
        resolved: Promise.resolve({
          serverId: "09876",
        }),
      } as any,
      original: {
        filetype: "text",
      } as any,
    }),
  ).toBe(context);

  expect(global.console.error).toHaveBeenLastCalledWith(
    "Incompatible file types. Original: text, Version: cedar}",
  );
});

test("valid remove", async () => {
  const fileOne = {
    filename: "file-one.cedarschema",
    filetype: "cedarschema",
    resolved: Promise.resolve({
      serverId: "1",
    }),
  };
  const fileTwo = {
    filename: "file-two.cedar",
    filetype: "cedar",
    resolved: Promise.resolve({
      serverId: "2",
    }),
  };
  const fileThree = {
    filename: "file-three.cedar",
    filetype: "cedar",
    resolved: Promise.resolve({
      serverId: "3",
    }),
  };
  const fileFour = {
    filename: "file-four.cedar",
    filetype: "cedar",
    resolved: Promise.resolve({
      serverId: "4",
    }),
  };
  const reportOne = {
    id: "1",
    sourceLocations: [
      {
        location: {
          source: fileOne,
        },
      },
    ],
  };
  const reportTwo = {
    id: "2",
    sourceLocations: [
      {
        location: {
          source: fileTwo,
        },
      },
    ],
  };
  const reportThree = {
    id: "3",
    sourceLocations: [
      {
        location: {
          source: fileThree,
        },
      },
    ],
  };
  const reportFour = {
    id: "4",
    sourceLocations: [
      {
        location: {
          source: fileFour,
        },
      },
    ],
  };

  const context = {
    files: [
      {
        original: fileOne,
        versions: [],
      },
      {
        original: fileTwo,
        versions: [fileThree, fileFour],
      },
    ] as any,
    reports: Promise.resolve<any[]>([
      reportOne,
      reportTwo,
      reportThree,
      reportFour,
    ]),
  } as any;

  // Remove single file
  let updated = reducer(context, {
    type: "remove",
    file: fileOne as any,
  });

  // Returns new object
  expect(updated).not.toBe(context);

  expect(updated.files).toHaveLength(1);
  expect(updated.files[0].original.filename).toBe("file-two.cedar");
  expect(updated.files[0].versions).toHaveLength(2);
  expect(updated.files[0].versions[0].filename).toBe("file-three.cedar");
  expect(updated.files[0].versions[1].filename).toBe("file-four.cedar");

  let reports = (await updated.reports) as Report[];
  expect(reports).toHaveLength(3);
  expect(reports[0].id).toBe("2");
  expect(reports[1].id).toBe("3");
  expect(reports[2].id).toBe("4");

  // Remove file with versions
  updated = reducer(context, {
    type: "remove",
    file: fileTwo as any,
  });
  expect(updated.files).toHaveLength(1);
  expect(updated.files[0].original.filename).toBe("file-one.cedarschema");
  expect(updated.files[0].versions).toHaveLength(0);

  reports = (await updated.reports) as Report[];
  expect(reports).toHaveLength(1);
  expect(reports[0].id).toBe("1");

  // Remove single version
  updated = reducer(context, {
    type: "remove",
    file: fileThree as any,
    original: fileTwo as any,
  });
  expect(updated.files).toHaveLength(2);
  expect(updated.files[0].original.filename).toBe("file-one.cedarschema");
  expect(updated.files[0].versions).toHaveLength(0);

  expect(updated.files[1].original.filename).toBe("file-two.cedar");
  expect(updated.files[1].versions).toHaveLength(1);
  expect(updated.files[1].versions[0].filename).toBe("file-four.cedar");

  reports = (await updated.reports) as Report[];
  expect(reports).toHaveLength(3);
  expect(reports[0].id).toBe("1");
  expect(reports[1].id).toBe("2");
  expect(reports[2].id).toBe("4");
});

test("invalid remove", () => {
  // No file
  const context: AnalysisGroup = {
    name: "group",
    files: [],
    diffs: {},
    verifyPending: false,
    verifyComplete: false,
  };

  expect(
    reducer(context, {
      type: "remove",
    }),
  ).toBe(context);

  expect(global.console.error).toHaveBeenLastCalledWith(
    'Invalid action: {"type":"remove"}',
  );
});

test("valid diff", async () => {
  let context = {
    diffs: [],
  } as any;

  // Returns new object
  expect(
    reducer(context, {
      type: "diff",
      diff: Promise.resolve(""),
      originalId: "one",
      updatedId: "two",
    }),
  ).not.toBe(context);

  // New original ID
  context = reducer(context, {
    type: "diff",
    diff: Promise.resolve("1"),
    originalId: "one",
    updatedId: "two",
  });
  expect(context.diffs).toHaveProperty("one");
  expect(context.diffs.one).toHaveProperty("two");
  expect(context.diffs.one.two).toBeDefined();
  expect(await context.diffs.one.two).toBe("1");

  // Original ID exists
  context = reducer(context, {
    type: "diff",
    diff: Promise.resolve("2"),
    originalId: "one",
    updatedId: "three",
  });
  expect(context.diffs).toHaveProperty("one");
  expect(context.diffs.one).toHaveProperty("two");
  expect(context.diffs.one).toHaveProperty("three");
  expect(context.diffs.one.two).toBeDefined();
  expect(context.diffs.one.three).toBeDefined();
  expect(await context.diffs.one.three).toBe("2");

  // Diff exists
  context = reducer(context, {
    type: "diff",
    diff: Promise.resolve("3"),
    originalId: "one",
    updatedId: "two",
  });
  expect(context.diffs).toHaveProperty("one");
  expect(context.diffs.one).toHaveProperty("two");
  expect(context.diffs.one).toHaveProperty("three");
  expect(context.diffs.one.two).toBeDefined();
  expect(context.diffs.one.three).toBeDefined();
  expect(await context.diffs.one.two).toBe("3");
});

test("invalid diff", () => {
  // No diff
  const context = {} as any;

  expect(
    reducer(context, {
      type: "diff",
      originalId: "1",
      updatedId: "2",
    }),
  ).toBe(context);

  expect(global.console.error).toHaveBeenLastCalledWith(
    'Invalid action: {"type":"diff","originalId":"1","updatedId":"2"}',
  );

  // Undefined original ID
  expect(
    reducer(context, {
      type: "diff",
      diff: Promise.resolve(""),
      updatedId: "2",
    }),
  ).toBe(context);

  expect(global.console.error).toHaveBeenLastCalledWith(
    'Invalid action: {"type":"diff","diff":{},"updatedId":"2"}',
  );

  // Empty original ID
  expect(
    reducer(context, {
      type: "diff",
      diff: Promise.resolve(""),
      originalId: "",
      updatedId: "2",
    }),
  ).toBe(context);

  expect(global.console.error).toHaveBeenLastCalledWith(
    'Invalid action: {"type":"diff","diff":{},"originalId":"","updatedId":"2"}',
  );

  // Undefined updated ID
  expect(
    reducer(context, {
      type: "diff",
      diff: Promise.resolve(""),
      originalId: "1",
    }),
  ).toBe(context);

  expect(global.console.error).toHaveBeenLastCalledWith(
    'Invalid action: {"type":"diff","diff":{},"originalId":"1"}',
  );

  // Empty updated ID
  expect(
    reducer(context, {
      type: "diff",
      diff: Promise.resolve(""),
      originalId: "1",
      updatedId: "",
    }),
  ).toBe(context);

  expect(global.console.error).toHaveBeenLastCalledWith(
    'Invalid action: {"type":"diff","diff":{},"originalId":"1","updatedId":""}',
  );
});

test("valid impact", async () => {
  let context = {} as any;

  // Returns new object
  expect(
    reducer(context, {
      type: "impact",
      impact: Promise.resolve({
        permitted: [],
        forbidden: [],
      }),
      originalId: "one",
      updatedId: "two",
    }),
  ).not.toBe(context);

  // New original ID
  context = reducer(context, {
    type: "impact",
    impact: Promise.resolve<any>({
      permitted: ["1"],
      forbidden: ["2"],
    }),
    originalId: "one",
    updatedId: "two",
  });
  expect(context.impacts).toHaveProperty("one");
  expect(context.impacts.one).toHaveProperty("two");
  expect(context.impacts.one.two).toBeDefined();
  expect(await context.impacts.one.two).toEqual({
    permitted: ["1"],
    forbidden: ["2"],
  });

  // Original ID exists
  context = reducer(context, {
    type: "impact",
    impact: Promise.resolve<any>({
      permitted: ["3"],
      forbidden: ["4"],
    }),
    originalId: "one",
    updatedId: "three",
  });
  expect(context.impacts).toHaveProperty("one");
  expect(context.impacts.one).toHaveProperty("two");
  expect(context.impacts.one).toHaveProperty("three");
  expect(context.impacts.one.two).toBeDefined();
  expect(context.impacts.one.three).toBeDefined();
  expect(await context.impacts.one.three).toEqual({
    permitted: ["3"],
    forbidden: ["4"],
  });

  // Diff exists
  context = reducer(context, {
    type: "impact",
    impact: Promise.resolve<any>({
      permitted: ["5"],
      forbidden: ["6"],
    }),
    originalId: "one",
    updatedId: "two",
  });
  expect(context.impacts).toHaveProperty("one");
  expect(context.impacts.one).toHaveProperty("two");
  expect(context.impacts.one).toHaveProperty("three");
  expect(context.impacts.one.two).toBeDefined();
  expect(context.impacts.one.three).toBeDefined();
  expect(await context.impacts.one.two).toEqual({
    permitted: ["5"],
    forbidden: ["6"],
  });
});

test("invalid impact", () => {
  // No impact
  const context = {} as any;

  expect(
    reducer(context, {
      type: "impact",
      originalId: "1",
      updatedId: "2",
    }),
  ).toBe(context);

  expect(global.console.error).toHaveBeenLastCalledWith(
    'Invalid action: {"type":"impact","originalId":"1","updatedId":"2"}',
  );

  // Undefined original ID
  expect(
    reducer(context, {
      type: "impact",
      impact: Promise.resolve<any>({}),
      updatedId: "2",
    }),
  ).toBe(context);

  expect(global.console.error).toHaveBeenLastCalledWith(
    'Invalid action: {"type":"impact","impact":{},"updatedId":"2"}',
  );

  // Empty original ID
  expect(
    reducer(context, {
      type: "impact",
      impact: Promise.resolve<any>({}),
      originalId: "",
      updatedId: "2",
    }),
  ).toBe(context);

  expect(global.console.error).toHaveBeenLastCalledWith(
    'Invalid action: {"type":"impact","impact":{},"originalId":"","updatedId":"2"}',
  );

  // Undefined updated ID
  expect(
    reducer(context, {
      type: "impact",
      impact: Promise.resolve<any>({}),
      originalId: "1",
    }),
  ).toBe(context);

  expect(global.console.error).toHaveBeenLastCalledWith(
    'Invalid action: {"type":"impact","impact":{},"originalId":"1"}',
  );

  // Empty updated ID
  expect(
    reducer(context, {
      type: "impact",
      impact: Promise.resolve<any>({}),
      originalId: "1",
      updatedId: "",
    }),
  ).toBe(context);

  expect(global.console.error).toHaveBeenLastCalledWith(
    'Invalid action: {"type":"impact","impact":{},"originalId":"1","updatedId":""}',
  );
});

test("valid update", () => {
  const update: AnalysisGroup = {
    name: "goop",
    files: [],
    diffs: {},
    verifyPending: true,
    verifyComplete: false,
  };

  expect(
    reducer(
      {
        name: "group",
        files: [],
        diffs: {},
        verifyPending: false,
        verifyComplete: false,
      },
      {
        type: "update",
        update,
      },
    ),
  ).toBe(update);
});

test("invalid update", () => {
  // No update
  const context: AnalysisGroup = {
    name: "group",
    files: [],
    diffs: {},
    verifyPending: false,
    verifyComplete: false,
  };

  expect(
    reducer(context, {
      type: "update",
    }),
  ).toBe(context);

  expect(global.console.error).toHaveBeenLastCalledWith(
    'Invalid action: {"type":"update"}',
  );
});
