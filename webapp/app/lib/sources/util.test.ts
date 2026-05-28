import hljs from "highlight.js";
import {
  getHighlightFunction,
  groupReportsBySourceLine,
  sortReportLinesByPrecedence,
} from "./util";

jest.mock("highlight.js", () => ({
  highlight: jest.fn().mockReturnValue({ value: "highlighted text" }),
}));

test("sort reports by precedence", () => {
  // No reports
  expect(sortReportLinesByPrecedence([])).toEqual({ reportsByPrecedence: [] });

  // Single report
  const one = {
    id: 1,
    report: {
      severity: "info",
    },
    loc: {
      offset: 15,
      len: 10,
    },
  };
  expect(sortReportLinesByPrecedence([one] as any[])).toEqual({
    reportsByPrecedence: [one],
    mostSevere: one,
    mostRelevant: one,
  });

  // Multiple reports
  const two = {
    id: 2,
    report: {
      severity: "err",
    },
    loc: {
      offset: 5,
      len: 30,
    },
  };
  const three = {
    id: 3,
    report: {
      severity: "warn",
    },
    loc: {
      offset: 15,
      len: 2,
    },
  };
  expect(sortReportLinesByPrecedence([one, two, three] as any[])).toEqual({
    reportsByPrecedence: [three, one, two],
    mostSevere: two,
    mostRelevant: three,
  });
  expect(sortReportLinesByPrecedence([three, two, one] as any[])).toEqual({
    reportsByPrecedence: [three, one, two],
    mostSevere: two,
    mostRelevant: three,
  });
  expect(sortReportLinesByPrecedence([two, three, one] as any[])).toEqual({
    reportsByPrecedence: [three, one, two],
    mostSevere: two,
    mostRelevant: three,
  });
  expect(sortReportLinesByPrecedence([three, one, two] as any[])).toEqual({
    reportsByPrecedence: [three, one, two],
    mostSevere: two,
    mostRelevant: three,
  });
});

test("group reports by line", () => {
  const file = {} as any;
  const lines = [
    /* 1 */ "the first line of the file",
    /* 2 */ "another fascinating line",
    /* 3 */ "the third line is probably the best line",
    /* 4 */ "another line won't hurt",
    /* 5 */ "     one final line to end the file",
  ];
  const content = lines.join("\n");

  // No reports
  expect(groupReportsBySourceLine(file, content)).toEqual({
    reportsByLine: {},
    lines,
  });
  expect(groupReportsBySourceLine(file, content, [])).toEqual({
    reportsByLine: {},
    lines,
  });

  // One report
  // the third line is probably the best line
  //                   ^                    ^
  const oneOffset = "the third line is ".length;
  const oneLength = "probably the best line".length;
  const one = {
    id: "1",
    sourceLocations: [
      {
        location: {
          source: file,
          offset: lines.slice(0, 3).join("\n").length + oneOffset + 1, // +1 for newline
          len: oneLength,
          startLoc: {
            line: 3,
            column: oneOffset,
          },
          endLoc: {
            line: 3,
            column: oneLength,
          },
        },
      },
    ],
  } as any;
  expect(groupReportsBySourceLine(file, content, [one])).toEqual({
    reportsByLine: {
      3: [
        {
          report: one,
          loc: one.sourceLocations[0].location,
          start: oneOffset - 1, // 0-index
          end: oneOffset + oneLength - 1, // 0-index
        },
      ],
    },
    lines,
  });

  // Newline at end of file
  expect(groupReportsBySourceLine(file, content + "\n", [one])).toEqual({
    reportsByLine: {
      3: [
        {
          report: one,
          loc: one.sourceLocations[0].location,
          start: oneOffset - 1, // 0-index
          end: oneOffset + oneLength - 1, // 0-index
        },
      ],
    },
    lines: [...lines, ""],
  });

  // Ignore invalid report locations
  const invalid = [
    {
      id: "2",
      sourceLocations: [
        {
          location: {
            ...one.sourceLocations[0].location,
            startLoc: undefined,
          },
        },
      ],
    },
    {
      id: "3",
      sourceLocations: [
        {
          location: {
            ...one.sourceLocations[0].location,
            endLoc: undefined,
          },
        },
      ],
    },
    {
      id: "4",
      sourceLocations: [
        {
          location: {
            ...one.sourceLocations[0].location,
            startLoc: {
              line: 2,
              column: 0,
            },
            endLoc: {
              line: 1,
              column: 0,
            },
          },
        },
      ],
    },
  ] as any[];
  expect(groupReportsBySourceLine(file, content, invalid)).toEqual({
    reportsByLine: {},
    lines,
  });

  // Many reports
  const many = [
    // Basic partial line report
    one,

    // Different file
    {
      id: "2",
      sourceLocations: [
        {
          location: {
            source: {},
          },
        },
      ],
    },

    // Report spans two entire lines (highighted as block, not partial)
    // the third line is probably the best line
    // ^
    // another line won't hurt
    //                        ^
    {
      id: "3",
      sourceLocations: [
        {
          location: {
            source: file,
            offset: lines.slice(0, 3).join("\n").length + 1, // +1 for newline
            len: lines[2].length + lines[3].length + 2, // +2 for newlines
            startLoc: {
              line: 3,
              column: 1,
            },
            endLoc: {
              line: 4,
              column: lines[3].length,
            },
          },
        },
      ],
    },

    // Report with multiple locations
    {
      id: "4",
      sourceLocations: [
        // Location spans one entire line, but is indented (not partial line)
        //      one final line to end the file
        //      ^                             ^
        {
          location: {
            source: file,
            offset: lines.slice(0, 4).join("\n").length + 6, // +1 for newline +5 for indent
            len: lines[4].length - 5, // -5 for indent
            startLoc: {
              line: 5,
              column: 6,
            },
            endLoc: {
              line: 5,
              column: lines[4].length - 5, // -5 for indent
            },
          },
        },

        // Location partially on two lines
        // the first line of the file
        //           ^
        // another fascinating line
        //       ^
        {
          location: {
            source: file,
            offset: "the first ".length, // 0-index
            len: "line of the file\n".length + "another".length,
            startLoc: {
              line: 1,
              column: "the first ".length + 1, // 1-index
            },
            endLoc: {
              line: 2,
              column: "another".length + 1, // 1-index
            },
          },
        },
      ],
    },
  ] as any[];
  expect(groupReportsBySourceLine(file, content, many)).toEqual({
    reportsByLine: {
      1: [
        {
          report: many[3],
          loc: many[3].sourceLocations[1].location,
          start: many[3].sourceLocations[1].location.offset,
          end: lines[0].length,
        },
      ],
      2: [
        {
          report: many[3],
          loc: many[3].sourceLocations[1].location,
          start: 0,
          end: many[3].sourceLocations[1].location.endLoc.column - 1, // 0-index
        },
      ],
      3: [
        {
          report: one,
          loc: one.sourceLocations[0].location,
          start: oneOffset - 1, // 0-index
          end: oneOffset + oneLength - 1, // 0-index
        },
        {
          report: many[2],
          loc: many[2].sourceLocations[0].location,
        },
      ],
      4: [
        {
          report: many[2],
          loc: many[2].sourceLocations[0].location,
        },
      ],
      5: [
        {
          report: many[3],
          loc: many[3].sourceLocations[0].location,
        },
      ],
    },
    lines,
  });

  // Special handling for block highlighting of entities report lines ending in ",\n"
  const entities = {
    filetype: "entities",
  } as any;
  const json = JSON.stringify(
    {
      entity: {
        list: ["item", "item"],
        object: {
          key: "value",
        },
      },
    },
    null,
    2,
  ).split("\n");
  const entitiesReport = {
    id: "5",
    sourceLocations: [
      // Block location
      //     "object": {
      //     ^
      //       "key": "value",
      //     },
      //     ^
      {
        location: {
          source: entities,
          offset: json.slice(0, 6).join("\n").length + 5, // +1 for newline, +4 for indent
          len: '"object": {'.length + json[7].length + "    }".length + 2, // +2 for newlines
          startLoc: {
            line: 7,
            column: 5,
          },
          endLoc: {
            line: 9,
            column: 5,
          },
        },
      },

      // Partial location
      //    "list": [
      //    ^    ^
      {
        location: {
          source: entities,
          offset: json.slice(0, 2).join("\n").length + 5, // +1 for newline, +4 for indent
          len: '"list"'.length,
          startLoc: {
            line: 3,
            column: 4,
          },
          endLoc: {
            line: 3,
            column: 10,
          },
        },
      },
    ],
  } as any;
  expect(
    groupReportsBySourceLine(entities, json.join("\n"), [entitiesReport]),
  ).toEqual({
    reportsByLine: {
      3: [
        {
          report: entitiesReport,
          loc: entitiesReport.sourceLocations[1].location,
          start: entitiesReport.sourceLocations[1].location.startLoc.column - 1, // 0-index
          end: entitiesReport.sourceLocations[1].location.endLoc.column - 1, // 0-index
        },
      ],
      7: [
        {
          report: entitiesReport,
          loc: entitiesReport.sourceLocations[0].location,
        },
      ],
      8: [
        {
          report: entitiesReport,
          loc: entitiesReport.sourceLocations[0].location,
        },
      ],
      9: [
        {
          report: entitiesReport,
          loc: entitiesReport.sourceLocations[0].location,
        },
      ],
    },
    lines: json,
  });
});

test("get highlight function for syntax", () => {
  const text = "text to highlight";
  expect(getHighlightFunction("text")(text)).toBe(text);
  expect(hljs.highlight).not.toHaveBeenCalled();

  expect(getHighlightFunction("cedar")(text)).toEqual("highlighted text");
  expect(hljs.highlight).toHaveBeenCalledTimes(1);
  expect(hljs.highlight).toHaveBeenCalledWith(text, { language: "cedar" });
});
