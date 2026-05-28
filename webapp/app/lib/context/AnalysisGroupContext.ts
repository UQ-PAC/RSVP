import { createContext, Dispatch, useContext } from "react";
import {
  AnalysisGroup,
  ChangeImpact,
  VerificationFile,
  VerificationFileDict,
  VersionDict,
} from "../types";
import { sortSources } from "../util";

export const emptyAnalysisGroup: AnalysisGroup = {
  name: "",
  files: [],
  diffs: {},
  verifyPending: false,
  verifyComplete: false,
};

interface AnalysisGroupAction {
  type: "add" | "remove" | "diff" | "impact" | "update";
  file?: VerificationFile;
  index?: number;
  original?: VerificationFile;
  diff?: Promise<string>;
  impact?: Promise<ChangeImpact>;
  originalId?: string;
  updatedId?: string;
  update?: AnalysisGroup;
}

export const AnalysisGroupContext =
  createContext<AnalysisGroup>(emptyAnalysisGroup);

export const AnalysisGroupDispatchContext = createContext<
  Dispatch<AnalysisGroupAction>
>(() => {});

/* istanbul ignore next */
export function useAnalysisGroup() {
  return useContext(AnalysisGroupContext);
}

/* istanbul ignore next */
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
    case "remove":
      return doRemove(context, action);
    case "diff":
      return addDiff(context, action);
    case "impact":
      return addImpact(context, action);
    case "update":
      return doUpdate(context, action);
  }
}

function doAdd(
  context: AnalysisGroup,
  action: AnalysisGroupAction,
): AnalysisGroup {
  if (!action.file) {
    console.error(`Invalid action: ${JSON.stringify(action)}`);
    return context;
  }

  // Copy current context
  const newContext: AnalysisGroup = {
    ...context,
  };

  let filename = action.file.file.name;

  // Add file as a version of another file
  if (action.original) {
    if (action.file.filetype !== action.original.filetype) {
      console.error(
        `Incompatible file types. Original: ${action.original.filetype}, Version: ${action.file.filetype}}`,
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
          versions: [...existing.versions, { ...action.file, filename }],
        },
      ]);
    }
  } else {
    // Make sure file name is unique in UI
    if (doesFileExist(filename, context)) {
      let i = 1;

      while (doesFileExist(`${filename} (${i})`, context)) {
        i++;
      }

      filename = `${filename} (${i})`;
    }

    // Add new standalone file
    newContext.files = sortSources([
      ...context.files,
      { original: { ...action.file, filename }, versions: [] },
    ]);
  }

  // Re-index by serverId
  newContext.byId = sortFilesById(newContext).then(({ all }) => all);

  return newContext;
}

function doRemove(
  context: AnalysisGroup,
  action: AnalysisGroupAction,
): AnalysisGroup {
  if (!action.file) {
    console.error(`Invalid action: ${JSON.stringify(action)}`);
    return context;
  }

  const newContext: AnalysisGroup = {
    ...context,
  };

  if (action.original) {
    // Remove version
    newContext.files = context.files.map(({ original, versions }) => {
      if (original === action.original) {
        return {
          original,
          versions: versions.filter((version) => version !== action.file),
        };
      }
      return { original, versions: [...versions] };
    });

    // Delete any associated reports
    newContext.reports = context.reports?.then((reports) =>
      reports.filter(
        (report) => report.sourceLocations[0]?.location.source !== action.file,
      ),
    );
  } else {
    // Remove standalone file (and all versions)
    const toRemove = context.files.find(
      (file) => file.original === action.file,
    );
    newContext.files = [...context.files.filter((file) => file !== toRemove)];

    // Delete any associated reports
    newContext.reports = context.reports?.then((reports) =>
      reports.filter((report) => {
        const source = report.sourceLocations[0]?.location.source;
        return (
          source !== toRemove?.original &&
          !toRemove?.versions.some((version) => source === version)
        );
      }),
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
  if (!action.diff || !action.originalId || !action.updatedId) {
    console.error(`Invalid action: ${JSON.stringify(action)}`);
    return context;
  }

  const newContext = { ...context };

  const newDiffs = { ...newContext.diffs };

  const newDiffsForFile = { ...newDiffs[action.originalId] };

  newDiffsForFile[action.updatedId] = action.diff;
  newDiffs[action.originalId] = newDiffsForFile;

  newContext.diffs = newDiffs;

  return newContext;
}

function addImpact(
  context: AnalysisGroup,
  action: AnalysisGroupAction,
): AnalysisGroup {
  if (!action.impact || !action.originalId || !action.updatedId) {
    console.error(`Invalid action: ${JSON.stringify(action)}`);
    return context;
  }

  const newContext = { ...context };

  const newImpacts = newContext.impacts ? { ...newContext.impacts } : {};

  const newImpactsForFile = { ...newImpacts[action.originalId] };

  newImpactsForFile[action.updatedId] = action.impact;
  newImpacts[action.originalId] = newImpactsForFile;

  newContext.impacts = newImpacts;

  return newContext;
}

function doUpdate(
  context: AnalysisGroup,
  action: AnalysisGroupAction,
): AnalysisGroup {
  if (!action.update) {
    console.error(`Invalid action: ${JSON.stringify(action)}`);
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
  return context.files.some((file) => file.original.filename === filename);
}

async function getServerId(file: VerificationFile): Promise<string> {
  return file.resolved.then((resolved) => resolved.serverId);
}
