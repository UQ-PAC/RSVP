import { sortSources } from "@/app/util";
import { verify } from "../../requests";
import {
  VerificationFile,
  VerificationRequest,
  Report,
  VersionedFile,
} from "../../types";
import { createContext, Dispatch, useContext } from "react";

type VerificationFileDict = { [id: string]: VerificationFile };
type VersionedFileList = VersionedFile[];

interface VerificationGroup {
  files: VersionedFileList;
  byId?: Promise<VerificationFileDict>;
  reports?: Promise<Report[]>;
}

interface VerificationState {
  [id: string]: VerificationGroup;
}

export const emptyVerificationGroup: VerificationGroup = {
  files: [],
};
export const emptyVerification: VerificationState = {};

interface VerificationAction {
  type: "add" | "move" | "remove" | "verify";
  group?: string;
  file?: VerificationFile;
  index?: number;
  original?: VerificationFile;
}

export const VerificationContext =
  createContext<VerificationState>(emptyVerification);

export const VerificationDispatchContext = createContext<
  Dispatch<VerificationAction>
>(() => {});

export function useVerification() {
  return useContext(VerificationContext);
}

export function useVerificationDispatch() {
  return useContext(VerificationDispatchContext);
}

export function verificationReducer(
  context: VerificationState,
  action: VerificationAction,
): VerificationState {
  switch (action.type) {
    case "verify":
      return doVerify(context);
    case "add":
      return doAdd(context, action);
    case "move":
      return doMove(context, action);
    case "remove":
      return doRemove(context, action);
  }
}

function doVerify(context: VerificationState): VerificationState {
  const newContext = {};

  // TODO: multiple versions
  // FIXME:
  Object.entries(context).forEach(([id, group]) => {
    const fileResolution = new Promise<{
      request: VerificationRequest;
      byId: VerificationFileDict;
      resolveFilenames: (report: Report) => Report;
    }>(async (resolve) => {
      const { policies, schemas, entities, invariants, all } =
        await sortFilesById(group);

      const request: VerificationRequest = {
        policyFiles: Object.keys(policies).map((id) => [{ version: "0", id }]),
        schemas: Object.keys(schemas),
        entities: Object.keys(entities),
        invariants: Object.keys(invariants),
      };

      const resolveFilenames = (report: Report): Report => ({
        ...report,
        primarySourceLocation: {
          ...report.primarySourceLocation,
          source: all[report.primarySourceLocation.file],
        },
        sourceLocations: report.sourceLocations.map((sourceLoc) => ({
          ...sourceLoc,
          source: all[sourceLoc.file],
        })),
      });

      resolve({
        request,
        byId: all,
        resolveFilenames,
      });
    });

    const newGroup = {
      ...group,
      byId: fileResolution.then(({ byId }) => byId),
      reports: fileResolution.then(({ request, resolveFilenames }) =>
        verify(request).then((reports) => reports.map(resolveFilenames)),
      ),
    };

    newContext[id] = newGroup;
  });
  return newContext;
}

function doAdd(
  context: VerificationState,
  action: VerificationAction,
): VerificationState {
  if (!action.file || !action.group) {
    console.error(
      `Invalid action: { file: ${action.file}, group: ${action.group} }`,
    );
    return context;
  }

  // Copy current context
  const group: VerificationGroup = context[action.group] ?? {
    ...emptyVerificationGroup,
  };
  const newContext = { ...context };
  const newGroup = { ...group };

  if (action.original) {
    if (action.file.filetype !== action.original.filetype) {
      console.error(
        `Incompatible file types. Original: ${action.original.filetype}, Version: ${action.file.filetype}}`,
      );
      return context;
    }

    // Add file as version of another file
    const existing = newGroup.files.find(
      (file) => file.original === action.original,
    );

    if (!existing) {
      console.error(
        "Adding version to nonexistent file: " + action.file.file.name,
      );
      return context;
    } else {
      newGroup.files = sortSources([
        ...group.files.filter((file) => file !== existing),
        {
          original: existing.original,
          versions: [...existing.versions, action.file],
        },
      ]);
    }
  } else {
    // Add new standalone file
    newGroup.files = sortSources([
      ...group.files,
      { original: action.file, versions: [] },
    ]);
  }

  // Re-index by serverId
  newGroup.byId = sortFilesById(newGroup).then(({ all }) => all);

  newContext[action.group] = newGroup;
  return newContext;
}

function doMove(
  context: VerificationState,
  action: VerificationAction,
): VerificationState {
  if (!action.file || !action.group || action.index === undefined) {
    console.error(
      `Invalid action: { file: ${action.file}, group: ${action.group}, index: ${action.index} }`,
    );
    return context;
  }

  const group: VerificationGroup = context[action.group];

  if (!group) {
    console.error("Moving file within non-existent group: " + action.group);
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
  context: VerificationState,
  action: VerificationAction,
): VerificationState {
  if (!action.file || !action.group) {
    console.error(
      `Invalid action: { file: ${action.file}, group: ${action.group} }`,
    );
    return context;
  }

  const group: VerificationGroup = context[action.group];

  if (!group) {
    console.error("Removing file from non-existent group: " + action.group);
    return context;
  }

  const newContext = { ...context };
  const newGroup = { ...group };

  if (action.original) {
    // Remove version
    const existing = newGroup.files.find(
      (file) => file.original === action.original,
    );

    if (!existing) {
      console.error(
        "Removing version of nonexistent file: " + action.file.file.name,
      );
      return context;
    } else {
      newGroup.files = [
        ...group.files.filter((file) => file !== existing),
        {
          original: existing.original,
          versions: [
            ...existing.versions.filter((version) => version !== action.file),
          ],
        },
      ];
    }
  } else {
    newGroup.files = [
      ...group.files.filter((file) => file.original !== action.file),
    ];
  }

  // Re-index by serverId
  newGroup.byId = sortFilesById(newGroup).then(({ all }) => all);

  newContext[action.group] = newGroup;
  return newContext;
}

async function sortFilesById(group: VerificationGroup): Promise<{
  policies: VerificationFileDict;
  schemas: VerificationFileDict;
  entities: VerificationFileDict;
  invariants: VerificationFileDict;
  all: VerificationFileDict;
}> {
  return new Promise(async (resolve) => {
    const sorted: { [type: string]: VerificationFileDict } = {
      cedar: {},
      cedarschema: {},
      entities: {},
      invariant: {},
    };

    const versions: VerificationFileDict = {};

    const mapId = async (file: VerificationFile) => {
      getServerId(file).then((id) => (sorted[file.filetype][id] = file));
    };

    await Promise.all(group.files.map((file) => mapId(file.original)));

    await Promise.all(
      group.files.flatMap((file) =>
        file.versions.map((version) =>
          getServerId(version).then((id) => (versions[id] = version)),
        ),
      ),
    );

    resolve({
      policies: sorted.cedar,
      schemas: sorted.cedarschema,
      entities: sorted.entities,
      invariants: sorted.invariant,
      all: {
        ...sorted.cedar,
        ...sorted.cedarschema,
        ...sorted.entities,
        ...sorted.invariant,
        ...versions,
      },
    });
  });
}

async function getServerId(file: VerificationFile): Promise<string> {
  return file.resolved.then((resolved) => resolved.serverId);
}
