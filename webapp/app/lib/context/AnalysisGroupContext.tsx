import { createContext, Dispatch, useContext } from "react";
import {
  AnalysisGroup,
  VerificationFile,
  VerificationFileDict,
  VersionDict,
} from "../../lib/types";
import { sortSources } from "../../lib/util";

export const emptyAnalysisGroup: AnalysisGroup = {
  name: "",
  files: [],
  diffs: {},
  verifyPending: false,
};

interface AnalysisGroupAction {
  type: "add" | "move" | "remove" | "diff" | "impact" | "update";
  file?: VerificationFile;
  index?: number;
  original?: VerificationFile;
  diff?: string;
  originalId?: string;
  updatedId?: string;
  update?: AnalysisGroup;
}

export const AnalysisGroupContext =
  createContext<AnalysisGroup>(emptyAnalysisGroup);

export const AnalysisGroupDispatchContext = createContext<
  Dispatch<AnalysisGroupAction>
>(() => {});

export function useAnalysisGroup() {
  return useContext(AnalysisGroupContext);
}

export function useAnalysisGroupDispatch() {
  return useContext(AnalysisGroupDispatchContext);
}

export function reducer(
  context: AnalysisGroup,
  action: AnalysisGroupAction,
): AnalysisGroup {
  switch (action.type) {
    case "add":
      return doAdd(context, action);
    case "move":
      return doMove(context, action);
    case "remove":
      return doRemove(context, action);
    case "diff":
    case "impact":
      return addDiff(context, action);
    case "update":
      return doUpdate(context, action);
  }
}

function doAdd(
  context: AnalysisGroup,
  action: AnalysisGroupAction,
): AnalysisGroup {
  if (!action.file) {
    console.error(`invalid action: ${JSON.stringify(action)}`);
    return context;
  }

  // Copy current context
  const newContext: AnalysisGroup = {
    ...context,
    verifyPending: false,
    verifyCompleted: undefined,
  };

  let filename = action.file.file.name;

  // Make sure file name is unique in UI
  if (doesFileExist(filename, context)) {
    let i = 1;

    while (doesFileExist(`${filename} (${i})`, context)) {
      i++;
    }

    filename = `${filename} (${i})`;
  }

  const toAdd = { ...action.file, filename };

  // Add file as a version of another file
  if (action.original) {
    if (toAdd.filetype !== action.original.filetype) {
      console.error(
        `Incompatible file types. Original: ${action.original.filetype}, Version: ${toAdd.filetype}}`,
      );
      return context;
    }

    // Add file as version of another file
    const existing = newContext.files.find(
      (file) => file.original === action.original,
    );

    if (!existing) {
      console.error("Adding version to nonexistent file: " + filename);
      return context;
    } else {
      newContext.files = sortSources([
        ...context.files.filter((file) => file !== existing),
        {
          original: existing.original,
          versions: [...existing.versions, toAdd],
        },
      ]);
    }
  } else {
    // Add new standalone file
    newContext.files = sortSources([
      ...context.files,
      { original: toAdd, versions: [] },
    ]);
  }

  // Re-index by serverId
  newContext.byId = sortFilesById(newContext).then(({ all }) => all);

  return newContext;
}

function doMove(
  context: AnalysisGroup,
  action: AnalysisGroupAction,
): AnalysisGroup {
  if (!action.file || action.index === undefined) {
    console.error(
      `Invalid action: { file: ${action.file}, index: ${action.index} }`,
    );
    return context;
  }

  return context;

  // TODO:
  // 1. Move out of version (+ sort sources)
  // 2. Move into version
  // 3. Move between versions

  // const newContext = { ...context };
  // const newGroup = { ...group };

  // // Add file to new position
  // if (action.original) {
  //   // Move to version
  //   const target = newGroup.files.find(
  //     (file) => file.original === action.original,
  //   );

  //   if (!target) {
  //     console.error(
  //       "Moving version to nonexistent file: " + action.file.file.name,
  //     );
  //     return context;
  //   } else {
  //     newGroup.files = [
  //       ...group.files.filter((file) => file !== existing),
  //       {
  //         original: existing.original,
  //         versions: [
  //           ...existing.versions.filter((version) => version !== action.file),
  //         ],
  //       },
  //     ];
  //   }
  // } else {
  //   newGroup.files = [...newGroup.files];
  // }

  // const isVersion = !newGroup.files.some(
  //   (file) => file.original === action.file,
  // );

  // // Remove file from current position
  // if (isVersion) {
  //   // Locate current original file
  //   const original = newGroup.files.find((file) =>
  //     file.versions.some((version) => version === action.file),
  //   );

  //   if (!original) {
  //     console.error("Moving nonexistent version: " + action.file.file.name);
  //     return context;
  //   } else {
  //     newGroup.files = [
  //       ...group.files.filter((file) => file !== original),
  //       {
  //         original: original.original,
  //         versions: [
  //           ...original.versions.filter((version) => version !== action.file),
  //         ],
  //       },
  //     ];
  //   }
  // } else {
  //   newGroup.files = [
  //     ...newGroup.files.filter((file) => file.original !== action.file),
  //   ];
  // }

  // newContext[action.group] = newGroup;
  // return newContext;
}

function doRemove(
  context: AnalysisGroup,
  action: AnalysisGroupAction,
): AnalysisGroup {
  if (!action.file) {
    console.error(`invalid action: ${JSON.stringify(action)}`);
    return context;
  }

  const newContext: AnalysisGroup = {
    ...context,
    verifyPending: false,
    verifyCompleted: undefined,
  };

  if (action.original) {
    // Remove version
    const existing = newContext.files.find(
      (file) => file.original === action.original,
    );

    if (!existing) {
      console.error(
        "Removing version of nonexistent file: " + action.file.filename,
      );
      return context;
    } else {
      newContext.files = [
        ...context.files.filter((file) => file !== existing),
        {
          original: existing.original,
          versions: [
            ...existing.versions.filter((version) => version !== action.file),
          ],
        },
      ];
    }
  } else {
    // Remove standalone file (and all versions)
    newContext.files = [
      ...context.files.filter((file) => file.original !== action.file),
    ];
    // Delete any associated reports
    newContext.reports = context.reports?.then((reports) =>
      reports.filter(
        (report) => report.sourceLocations[0]?.location.source !== action.file,
      ),
    );
  }

  // Re-index by serverId
  newContext.byId = sortFilesById(newContext).then(({ all }) => all);

  return newContext;
}

function addDiff(
  context: AnalysisGroup,
  action: AnalysisGroupAction,
): AnalysisGroup {
  if (action.diff === undefined || !action.originalId || !action.updatedId) {
    console.error(`Invalid action: ${JSON.stringify(action)}`);
    return context;
  }

  const newContext = { ...context };

  const newDiffs =
    action.type === "diff"
      ? { ...newContext.diffs }
      : { ...newContext.impacts };

  if (!newDiffs) {
    return context;
  }

  const newDiffsForFile = { ...newDiffs[action.originalId] };

  newDiffsForFile[action.updatedId] = action.diff;
  newDiffs[action.originalId] = newDiffsForFile;

  if (action.type === "diff") {
    newContext.diffs = newDiffs;
  } else {
    newContext.impacts = newDiffs;
  }

  return newContext;
}

function doUpdate(
  context: AnalysisGroup,
  action: AnalysisGroupAction,
): AnalysisGroup {
  if (!action.update) {
    console.error(`bad update: ${JSON.stringify(action)}`);
    return context;
  }

  return action.update;
}

export async function sortFilesById(group: AnalysisGroup): Promise<{
  policies: VerificationFileDict;
  schemas: VerificationFileDict;
  entities: VerificationFileDict;
  invariants: VerificationFileDict;
  versions: VersionDict;
  all: VerificationFileDict;
}> {
  return new Promise((resolve) => {
    const sorted: { [type: string]: VerificationFileDict } = {
      cedar: {},
      cedarschema: {},
      entities: {},
      invariant: {},
    };

    const versions: VersionDict = {};
    const all: VerificationFileDict = {};

    const mapId = async (file: VerificationFile) => {
      getServerId(file).then((id) => {
        sorted[file.filetype][id] = file;
        if (file.filetype === "cedar") {
          versions[id] = [];
        }
      });
    };

    Promise.all(group.files.map((file) => mapId(file.original)))
      .then(() =>
        Promise.all(
          group.files
            .map((file) =>
              getServerId(file.original).then((originalId) => {
                if (file.versions.length) {
                  versions[originalId] = [];
                }
                return file.versions.map((version) =>
                  getServerId(version).then((id) => {
                    all[id] = version;
                    versions[originalId].push(id);
                  }),
                );
              }),
            )
            .flat(),
        ),
      )
      .then(() => {
        resolve({
          policies: sorted.cedar,
          schemas: sorted.cedarschema,
          entities: sorted.entities,
          invariants: sorted.invariant,
          versions,
          all: {
            ...sorted.cedar,
            ...sorted.cedarschema,
            ...sorted.entities,
            ...sorted.invariant,
            ...all,
          },
        });
      });
  });
}

function doesFileExist(filename: string, context: AnalysisGroup): boolean {
  return context.files.some(
    (file) =>
      file.original.filename === filename ||
      file.versions.some((version) => version.filename === filename),
  );
}

async function getServerId(file: VerificationFile): Promise<string> {
  return file.resolved.then((resolved) => resolved.serverId);
}
