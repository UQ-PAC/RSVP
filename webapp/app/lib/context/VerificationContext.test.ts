import { waitFor } from "@testing-library/react";
import { publish } from "../events";
import { remove, verify } from "../requests";
import { Report } from "../types";
import { checkAnalysisGroup } from "../util";
import { emptyAnalysisGroup } from "./AnalysisGroupContext";
import { verificationReducer, VerificationState } from "./VerificationContext";

var analysisGroupError = false;
var reports = [
  {
    sourceLocations: [
      {
        message: "loc1",
        location: {
          file: "p1",
        },
      },
      {
        message: "loc2",
        location: {
          file: "p4",
        },
      },
      {
        message: "loc3",
        location: {
          file: "e1",
        },
      },
    ],
  },
  {
    sourceLocations: [
      {
        message: "loc",
        location: {
          file: "s2",
        },
      },
    ],
  },
];

var policies = {
  p1: {
    id: "p1",
  },
  p2: {
    id: "p2",
  },
  p3: {
    id: "p3",
  },
};
var schemas = {
  s1: {
    id: "s1",
  },
  s2: {
    id: "s2",
  },
};
var entities = {
  e1: {
    id: "e1",
  },
};
var versions = {
  p4: {
    id: "p4",
  },
  p5: {
    id: "p5",
  },
};

var verifyResult;

jest.mock("../events", () => ({
  publish: jest.fn(),
}));

jest.mock("../requests", () => ({
  remove: jest.fn(),
  verify: jest.fn(() => verifyResult),
}));

jest.mock("../util", () => ({
  checkAnalysisGroup: jest.fn(() => ({ error: analysisGroupError })),
}));

jest.mock("./AnalysisGroupContext", () => {
  return {
    ...jest.requireActual("./AnalysisGroupContext"),
    sortFilesById: jest.fn(() => ({
      policies,
      schemas,
      entities,
      invariants: {},
      versions: {
        p1: ["p4", "p5"],
        p2: [],
        p3: [],
      },
      all: {
        ...policies,
        ...versions,
        ...schemas,
        ...entities,
      },
    })),
  };
});

let consoleErr;

beforeAll(() => {
  consoleErr = global.console.error;
  global.console.error = jest.fn();
});

beforeEach(() => {
  (global.console.error as any)?.mockClear?.();
  (publish as any)?.mockClear();
  (remove as any)?.mockClear();
  (verify as any)?.mockClear();
  (checkAnalysisGroup as any)?.mockClear();
  analysisGroupError = false;
});

afterAll(() => {
  global.console.error = consoleErr;
});

test("valid add", () => {
  expect(
    verificationReducer(
      {},
      {
        type: "add",
        group: "my group",
      },
    ),
  ).toEqual({
    "my group": {
      ...emptyAnalysisGroup,
      name: "my group",
    },
  });
});

test("invalid add", () => {
  const context = {
    group: {
      ...emptyAnalysisGroup,
      name: "group",
    },
  };

  // No group
  expect(
    verificationReducer(context, {
      type: "add",
    }),
  ).toBe(context);

  expect(global.console.error).toHaveBeenCalledWith(
    'Invalid action: {"type":"add"}',
  );

  // Group already exists
  expect(
    verificationReducer(context, {
      type: "add",
      group: "group",
    }),
  ).toBe(context);

  expect(global.console.error).toHaveBeenCalledWith(
    'Group "group" already exists',
  );
});

test("valid remove", async () => {
  // No files
  expect(
    verificationReducer(
      {
        group: {
          ...emptyAnalysisGroup,
          name: "group",
        },
      },
      {
        type: "remove",
        group: "group",
      },
    ),
  ).toEqual({});

  expect(remove).not.toHaveBeenCalled();

  // With files
  expect(
    verificationReducer(
      {
        one: {
          ...emptyAnalysisGroup,
          name: "one",
        },
        two: {
          ...emptyAnalysisGroup,
          name: "two",
          files: [
            {
              original: {
                resolved: Promise.resolve({ serverId: "12345" }),
              },
              versions: [],
            },
            {
              original: {
                resolved: Promise.resolve({ serverId: "67890" }),
              },
              versions: [
                {
                  resolved: Promise.resolve({ serverId: "54321" }),
                },
              ],
            },
          ] as any[],
        },
      },
      {
        type: "remove",
        group: "two",
      },
    ),
  ).toEqual({
    one: {
      ...emptyAnalysisGroup,
      name: "one",
    },
  });

  await waitFor(() => {
    expect(remove).toHaveBeenCalledTimes(3);
  });
  expect(remove).toHaveBeenCalledWith("12345");
  expect(remove).toHaveBeenCalledWith("67890");
  expect(remove).toHaveBeenCalledWith("54321");
});

test("invalid remove", () => {
  const context = {
    group: {
      ...emptyAnalysisGroup,
      name: "group",
    },
  };

  // No group
  expect(
    verificationReducer(context, {
      type: "remove",
    }),
  ).toBe(context);

  expect(global.console.error).toHaveBeenCalledWith(
    'Invalid action: {"type":"remove"}',
  );

  // Group doesn't exist
  expect(
    verificationReducer(context, {
      type: "remove",
      group: "my group",
    }),
  ).toBe(context);

  expect(global.console.error).toHaveBeenCalledWith(
    'Group "my group" doesn\'t exist.',
  );
});

test("valid verify", async () => {
  verifyResult = Promise.resolve(reports);

  const context: VerificationState = {
    one: {
      ...emptyAnalysisGroup,
      name: "one",
    },
    two: {
      ...emptyAnalysisGroup,
      name: "two",
    },
    three: {
      ...emptyAnalysisGroup,
      name: "three",
      verifyComplete: true,
    },
  };

  const { one, two, three } = verificationReducer(context, { type: "verify" });
  expect(one.verifyPending).toBeTruthy();
  expect(one.verifyComplete).toBeFalsy();

  expect(two.verifyPending).toBeTruthy();
  expect(two.verifyComplete).toBeFalsy();

  expect(three.verifyPending).toBeTruthy();
  expect(three.verifyComplete).toBeFalsy();

  // Check source locations resolved
  const result = (await one.reports) as Report[];

  expect(result).toBeDefined();
  expect(result).toHaveLength(2);
  expect(result[0].sourceLocations).toEqual([
    {
      message: "loc1",
      location: {
        file: "p1",
        source: policies.p1,
      },
    },
    {
      message: "loc2",
      location: {
        file: "p4",
        source: versions.p4,
      },
    },
    {
      message: "loc3",
      location: {
        file: "e1",
        source: entities.e1,
      },
    },
  ]);

  expect(result[1].sourceLocations).toEqual([
    {
      message: "loc",
      location: {
        file: "s2",
        source: schemas.s2,
      },
    },
  ]);

  await waitFor(() => {
    expect(publish).toHaveBeenCalledWith("verificationComplete");
  });

  expect(verify).toHaveBeenCalled();
});

test("invalid verify", async () => {
  analysisGroupError = true;

  const context: VerificationState = {
    one: {
      ...emptyAnalysisGroup,
      name: "one",
    },
    two: {
      ...emptyAnalysisGroup,
      name: "two",
    },
  };

  let { one, two } = verificationReducer(context, { type: "verify" });
  expect(publish).toHaveBeenCalledWith("verificationPending");
  expect(one).toBe(context.one);
  expect(two).toBe(context.two);

  analysisGroupError = false;
  verifyResult = Promise.reject();

  ({ one, two } = verificationReducer(context, { type: "verify" }));
  expect(one).not.toBe(context.one);
  expect(two).not.toBe(context.two);

  await waitFor(() => {
    expect(publish).toHaveBeenCalledWith("verificationError");
  });

  expect(verify).toHaveBeenCalled();
});

test("complete", () => {
  expect(
    verificationReducer(
      {
        one: { ...emptyAnalysisGroup },
        two: { ...emptyAnalysisGroup, verifyPending: true },
        three: {
          ...emptyAnalysisGroup,
          verifyPending: true,
          verifyComplete: true,
        },
        four: { ...emptyAnalysisGroup, verifyComplete: true },
      },
      {
        type: "complete",
      },
    ),
  ).toEqual({
    one: { ...emptyAnalysisGroup, verifyComplete: true },
    two: { ...emptyAnalysisGroup, verifyComplete: true },
    three: { ...emptyAnalysisGroup, verifyComplete: true },
    four: { ...emptyAnalysisGroup, verifyComplete: true },
  });
});

test("valid update", () => {
  const initial = {
    one: { ...emptyAnalysisGroup, name: "one" },
    two: { ...emptyAnalysisGroup, name: "two" },
  };

  // Update existing group
  expect(
    verificationReducer(initial, {
      type: "update",
      update: {
        ...emptyAnalysisGroup,
        name: "two",
        files: [
          {
            original: {
              filename: "file.txt",
            },
          },
          {
            original: {
              filename: "another-file.txt",
            },
          },
        ] as any[],
      },
    }),
  ).toEqual({
    ...initial,
    two: {
      ...emptyAnalysisGroup,
      name: "two",
      files: [
        {
          original: {
            filename: "file.txt",
          },
        },
        {
          original: {
            filename: "another-file.txt",
          },
        },
      ],
    },
  });

  // Add new group
  expect(
    verificationReducer(initial, {
      type: "update",
      update: {
        ...emptyAnalysisGroup,
        name: "three",
        files: [
          {
            original: {
              filename: "file.txt",
            },
          },
          {
            original: {
              filename: "another-file.txt",
            },
          },
        ] as any[],
      },
    }),
  ).toEqual({
    ...initial,
    three: {
      ...emptyAnalysisGroup,
      name: "three",
      files: [
        {
          original: {
            filename: "file.txt",
          },
        },
        {
          original: {
            filename: "another-file.txt",
          },
        },
      ],
    },
  });
});

test("invalid update", () => {
  const initial = {
    one: { ...emptyAnalysisGroup },
    two: { ...emptyAnalysisGroup },
  };

  expect(
    verificationReducer(initial, {
      type: "update",
    }),
  ).toBe(initial);

  expect(global.console.error).toHaveBeenCalledWith(
    'Invalid action: {"type":"update"}',
  );
});
