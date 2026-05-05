/* eslint-disable @typescript-eslint/no-explicit-any */
import { Report, VersionedFile } from "./types";
import { getFileType, sortReports, sortSources } from "./util";

test("sort reports", () => {
  const report = (id: string, offset: number): Report => ({
    id,
    primarySourceLocation: {
      file: "bar",
      offset,
      len: 0,
    },
    sourceLocations: [],
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
});

test("sort sources", () => {
  const source = (id: number, name: string): VersionedFile => ({
    original: {
      id,
      file: {
        name,
      } as any,
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
  expect(getFileType(file("some.silly.file.json"))).toBe("entities");
  expect(getFileType(file("some.silly.file.invariant"))).toBe("invariant");
  expect(getFileType(file("some.silly.file.pdf"))).toBe("text");
  expect(getFileType(file("cedar"))).toBe("text");
  expect(getFileType(file("cedarschema"))).toBe("text");
  expect(getFileType(file("json"))).toBe("text");
  expect(getFileType(file("invariant"))).toBe("text");
});

test("get source identifier", () => {});

//   test("get source string", () => {});
