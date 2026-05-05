/* eslint-disable @typescript-eslint/no-explicit-any */
import { verify } from "./requests";
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

beforeAll(() => {
  globalFetchBackup = global.fetch;

  global.fetch = jest.fn(() =>
    Promise.resolve({
      json: () => Promise.resolve([reportMock]),
      text: () => Promise.resolve("text data"),
      ok: true,
      status: 200,
    } as any),
  );
});

test("upload", async () => {});

test("verify", async () => {
  const request: VerificationRequest = {
    policies: [],
    schemas: [],
    entities: [],
    invariants: [],
  };

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
});

afterAll(() => {
  global.fetch = globalFetchBackup;
});
