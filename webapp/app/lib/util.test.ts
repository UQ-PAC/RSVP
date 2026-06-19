import { Report, VersionedFile } from "./types";
import {
  checkAnalysisGroup,
  getFileType,
  getSourceIdentifier,
  getSourceStr,
  sortReports,
  sortSources,
} from "./util";

test("sort reports", () => {
  const report = (id: string, offset?: number): Report => ({
    id,
    sourceLocations:
      offset !== undefined
        ? [
            {
              message: "",
              location: {
                file: "bar",
                offset,
                len: 0,
              },
            },
          ]
        : [],
    severity: "info",
    message: "",
  });

  expect(sortReports([])).toEqual([]);

  expect(sortReports([report("A", 0)])).toEqual([report("A", 0)]);
  expect(sortReports([report("A", 0), report("B", 1)])).toEqual([
    report("A", 0),
    report("B", 1),
  ]);
  expect(sortReports([report("B", 1), report("A", 0)])).toEqual([
    report("A", 0),
    report("B", 1),
  ]);
  expect(sortReports([report("A", 0), report("B", 0)])).toEqual([
    report("A", 0),
    report("B", 0),
  ]);

  expect(sortReports([report("B"), report("A")])).toEqual([
    report("B"),
    report("A"),
  ]);

  expect(sortReports([report("B"), report("A", 0)])).toEqual([
    report("A", 0),
    report("B"),
  ]);

  expect(sortReports([report("B", 0), report("A")])).toEqual([
    report("B", 0),
    report("A"),
  ]);
});

test("sort sources", () => {
  const source = (id: number, name: string): VersionedFile => ({
    original: {
      id,
      filename: name,
    } as any,
    versions: [],
  });

  expect(sortSources([])).toEqual([]);

  expect(sortSources([source(0, "name")])).toEqual([source(0, "name")]);
  expect(sortSources([source(0, "aardvark"), source(1, "anteater")])).toEqual([
    source(0, "aardvark"),
    source(1, "anteater"),
  ]);
  expect(sortSources([source(0, "Zebra"), source(1, "donkey")])).toEqual([
    source(1, "donkey"),
    source(0, "Zebra"),
  ]);
  expect(sortSources([source(0, "name"), source(1, "name")])).toEqual([
    source(0, "name"),
    source(1, "name"),
  ]);
});

test("get file type", () => {
  const file = (name: string): File =>
    ({
      name,
    }) as any;

  expect(getFileType(file("some.silly.file.cedar"))).toBe("cedar");
  expect(getFileType(file("some.silly.file.cedarschema"))).toBe("cedarschema");
  expect(getFileType("some.silly.file.json")).toBe("entities");
  expect(getFileType("some.silly.file.invariant")).toBe("invariant");
  expect(getFileType(file("some.silly.file.pdf"))).toBe("text");
  expect(getFileType(file("cedar"))).toBe("text");
  expect(getFileType(file("cedarschema"))).toBe("text");
  expect(getFileType(file("json"))).toBe("text");
  expect(getFileType(file("invariant"))).toBe("text");
});

test("get source identifier", () => {
  expect(
    getSourceIdentifier({
      file: "",
      offset: 0,
      len: 0,
    }),
  ).toBe(":0:0");
  expect(
    getSourceIdentifier({
      file: "foo",
      offset: 1234,
      len: 5678,
    }),
  ).toBe("foo:1234:5678");
});

test("get source string", () => {
  const baseLoc = {
    file: "",
    offset: 0,
    len: 0,
  };

  const loc = (line: number, column: number) => ({
    ...baseLoc,
    startLoc: {
      line,
      column,
    },
  });

  expect(getSourceStr(loc(0, 0))).toBe("Line 0, column 0");
  expect(getSourceStr(loc(5678, 1234))).toBe("Line 5678, column 1234");
  expect(getSourceStr(baseLoc)).toBe("Line undefined, column undefined");
});

test("checks analysis group", () => {
  // valid
  expect(
    checkAnalysisGroup([
      {
        original: { filetype: "cedar" },
      },
      {
        original: { filetype: "cedarschema" },
      },
      {
        original: { filetype: "entities" },
      },
    ] as any),
  ).toEqual({
    hasPolicy: true,
    hasSchema: true,
    hasEntities: true,
    error: false,
  });

  expect(
    checkAnalysisGroup([
      {
        original: { filetype: "cedar" },
      },
      {
        original: { filetype: "cedarschema" },
      },
      {
        original: { filetype: "entities" },
      },
      {
        original: { filetype: "cedar" },
      },
      {
        original: { filetype: "invariant" },
      },
      {
        original: { filetype: "text" },
      },
    ] as any),
  ).toEqual({
    hasPolicy: true,
    hasSchema: true,
    hasEntities: true,
    error: false,
  });

  // missing policy
  expect(
    checkAnalysisGroup([
      {
        original: { filetype: "cedarschema" },
      },
      {
        original: { filetype: "entities" },
      },
      {
        original: { filetype: "invariant" },
      },
      {
        original: { filetype: "cedarschema" },
      },
    ] as any),
  ).toEqual({
    hasPolicy: false,
    hasSchema: true,
    hasEntities: true,
    error: true,
  });

  // missing schema
  expect(
    checkAnalysisGroup([
      {
        original: { filetype: "cedar" },
      },
      {
        original: { filetype: "entities" },
      },
      {
        original: { filetype: "invariant" },
      },
      {
        original: { filetype: "cedar" },
      },
    ] as any),
  ).toEqual({
    hasPolicy: true,
    hasSchema: false,
    hasEntities: true,
    error: true,
  });

  // missing entities
  expect(
    checkAnalysisGroup([
      {
        original: { filetype: "cedar" },
      },
      {
        original: { filetype: "cedarschema" },
      },
      {
        original: { filetype: "invariant" },
      },
      {
        original: { filetype: "cedar" },
      },
    ] as any),
  ).toEqual({
    hasPolicy: true,
    hasSchema: true,
    hasEntities: false,
    error: true,
  });

  // missing all
  expect(
    checkAnalysisGroup([
      {
        original: { filetype: "text" },
      },
      {
        original: { filetype: "invariant" },
      },
      {
        original: { filetype: "invariant" },
      },
    ] as any),
  ).toEqual({
    hasPolicy: false,
    hasSchema: false,
    hasEntities: false,
    error: true,
  });
});
