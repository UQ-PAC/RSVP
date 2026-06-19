import { diff, impact, remove, upload, verify } from "./requests";
import { Report, VerificationRequest } from "./types";
import { sortReports } from "./util";

jest.mock("./util", () => ({
  sortReports: jest.fn().mockImplementation((reports) => reports),
}));

const reportMock: Report = {
  id: "foo",
  sourceLocations: [],
  severity: "info",
  message: "",
};

let globalFetchBackup;
let consoleErr;

beforeAll(() => {
  globalFetchBackup = global.fetch;
  consoleErr = global.console.error;

  global.console.error = jest.fn();
});

beforeEach(() => {
  (global.console.error as any)?.mockClear?.();
  (global.fetch as any)?.mockClear?.();
});

test("upload", async () => {
  global.fetch = jest.fn(() =>
    Promise.resolve({
      text: () => Promise.resolve("12345"),
      ok: true,
      status: 200,
    } as any),
  );

  const uploaded = await upload({} as any);
  expect(uploaded.serverId).toEqual("12345");

  expect(await uploaded.content).toEqual("12345");

  expect(global.fetch).toHaveBeenCalledTimes(2);
  expect(global.fetch).toHaveBeenNthCalledWith(2, "/api/file/12345");
});

test("remove", async () => {
  global.fetch = jest.fn(() =>
    Promise.resolve({
      ok: true,
      status: 200,
    } as any),
  );

  expect(await remove("12345")).toBe(true);
  expect(global.fetch).toHaveBeenCalledTimes(1);

  global.fetch = jest.fn(() =>
    Promise.resolve({
      ok: false,
      status: 400,
    } as any),
  );

  expect(await remove("12345")).toBe(false);
  expect(global.fetch).toHaveBeenCalledTimes(1);
});

test("verify", async () => {
  const request: VerificationRequest = {
    policies: [],
    schemas: [],
    entities: [],
    invariants: [],
  };

  // Success
  global.fetch = jest.fn(() =>
    Promise.resolve({
      json: () => Promise.resolve([reportMock]),
      ok: true,
      status: 200,
    } as any),
  );

  expect(await verify(request)).toEqual([reportMock]);
  expect(global.fetch).toHaveBeenCalledTimes(1);
  expect(global.fetch).toHaveBeenCalledWith("/api/verify", {
    headers: {
      Accept: "application/json",
      "Content-Type": "application/json",
    },
    method: "POST",
    body: JSON.stringify(request),
  });
  expect(sortReports).toHaveBeenCalledTimes(1);
  expect(sortReports).toHaveBeenCalledWith([reportMock]);

  // Server error
  global.fetch = jest.fn(() =>
    Promise.resolve({
      ok: false,
      status: 500,
    } as any),
  );

  expect(await verify(request)).toEqual([]);
  expect(sortReports).toHaveBeenCalledTimes(1);
  expect(global.console.error).toHaveBeenCalledWith({ ok: false, status: 500 });
});

test("diff", async () => {
  // Success
  global.fetch = jest.fn(() =>
    Promise.resolve({
      text: () => Promise.resolve("I'm a diff!"),
      ok: true,
      status: 200,
    } as any),
  );

  const request = [
    {
      id: "12345",
      name: "file.txt",
    },
    {
      id: "67890",
      name: "file.txt",
    },
  ];

  expect(await diff(request[0], request[1])).toEqual("I'm a diff!");

  expect(global.fetch).toHaveBeenCalled();
  expect(global.fetch).toHaveBeenCalledWith(
    "/api/diff?original=12345&originalName=file.txt&updated=67890&updatedName=file.txt",
  );

  // Bad request
  global.fetch = jest.fn(() =>
    Promise.resolve({
      ok: false,
      status: 400,
    } as any),
  );

  expect(await diff(request[0], request[1])).toEqual("");
  expect(global.fetch).toHaveBeenCalled();
  expect(global.console.error).toHaveBeenCalledWith({ ok: false, status: 400 });
});

test("impact", async () => {
  const result = {
    permitted: [{ summary: "permitted", locations: [] }],
    forbidden: [{ summary: "forbidden", locations: [] }],
  };

  // Success
  global.fetch = jest.fn(() =>
    Promise.resolve({
      json: () => Promise.resolve(result),
      ok: true,
      status: 200,
    } as any),
  );

  expect(await impact("12345", "67890")).toEqual(result);

  expect(global.fetch).toHaveBeenCalled();
  expect(global.fetch).toHaveBeenCalledWith(
    "/api/impact?original=12345&updated=67890",
  );

  // Bad request
  global.fetch = jest.fn(() =>
    Promise.resolve({
      ok: false,
      status: 400,
    } as any),
  );

  expect(await impact("12345", "67890")).toEqual({
    permitted: [],
    forbidden: [],
  });
  expect(global.fetch).toHaveBeenCalled();
  expect(global.console.error).toHaveBeenCalledWith({ ok: false, status: 400 });
});

afterAll(() => {
  global.fetch = globalFetchBackup;
  global.console.error = consoleErr;
});
