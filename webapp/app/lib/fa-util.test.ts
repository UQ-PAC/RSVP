import { getExpandIcon, getFileIcon } from "./fa-util";

jest.mock("@fortawesome/free-regular-svg-icons", () => ({
  faSquareMinus: "faRegularSquareMinus",
  faSquarePlus: "faRegularSquarePlus",
}));

jest.mock("@fortawesome/free-solid-svg-icons", () => ({
  faBarsStaggered: "faBarsStaggered",
  faCheckDouble: "faCheckDouble",
  faDatabase: "faDatabase",
  faFileLines: "faFileLines",
  faLock: "faLock",
  faSquareMinus: "faSolidSquareMinus",
  faSquarePlus: "faSolidSquarePlus",
}));

test("gets file icon", () => {
  expect(getFileIcon("cedar")).toEqual("faLock");
  expect(getFileIcon("cedarschema")).toEqual("faBarsStaggered");
  expect(getFileIcon("entities")).toEqual("faDatabase");
  expect(getFileIcon("invariant")).toEqual("faCheckDouble");
  expect(getFileIcon("text")).toEqual("faFileLines");
  expect(getFileIcon(undefined)).toEqual("faFileLines");
});

test("gets expand icon", () => {
  expect(getExpandIcon(true, true)).toEqual("faSolidSquareMinus");
  expect(getExpandIcon(true, false)).toEqual("faSolidSquarePlus");
  expect(getExpandIcon(false, true)).toEqual("faRegularSquareMinus");
  expect(getExpandIcon(false, false)).toEqual("faRegularSquarePlus");
});
