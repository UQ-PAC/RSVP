import { verify } from "../../requests";
import {
  VerificationFile,
  VerificationRequest,
  Report,
  VersionedFile,
} from "../../types";
import { createContext, Dispatch, useContext } from "react";

type VerificationFileList = VerificationFile[];
type VerificationFileDict = { [id: string]: VerificationFile };
type VersionedFileDict = { [id: string]: VersionedFile };

interface VerificationGroup {
  cedar: VerificationFileList; // TODO: multiple versions
  cedarschema: VerificationFileList;
  entities: VerificationFileList;
  invariant: VerificationFileList;
  versioned: VersionedFileDict;
  byId?: Promise<VerificationFileDict>;
  reports?: Promise<Report[]>;
}

interface VerificationState {
  [id: string]: VerificationGroup;
}

export const emptyVerificationGroup: VerificationGroup = {
  cedar: [],
  cedarschema: [],
  entities: [],
  invariant: [],
  versioned: {},
};
export const emptyVerification: VerificationState = {};

interface VerificationAction {
  type: "add" | "remove" | "version" | "verify";
  group?: string;
  file?: VerificationFile;
  original?: { file: VerificationFile; serverId: string }; // TODO: index
}

async function getServerId(file): Promise<string> {
  return file.resolved.then((resolved) => resolved.serverId);
}

async function sortFilesById(group: VerificationGroup): Promise<{
  policies: VerificationFileDict;
  schemas: VerificationFileDict;
  entities: VerificationFileDict;
  invariants: VerificationFileDict;
  all: VerificationFileDict;
}> {
  return new Promise(async (resolve) => {
    const policies: VerificationFileDict = {};
    const schemas: VerificationFileDict = {};
    const entities: VerificationFileDict = {};
    const invariants: VerificationFileDict = {};

    await Promise.all([
      ...group.cedar.map((file) =>
        getServerId(file).then((id) => (policies[id] = file)),
      ),
      ...group.cedarschema.map((file) =>
        getServerId(file).then((id) => (schemas[id] = file)),
      ),
      ...group.entities.map((file) =>
        getServerId(file).then((id) => (entities[id] = file)),
      ),
      ...group.invariant.map((file) =>
        getServerId(file).then((id) => (invariants[id] = file)),
      ),
      ...Object.values(group.versioned)
        .flatMap((versioned) => versioned.versions)
        .map((file) => getServerId(file).then((id) => (policies[id] = file))),
    ]);

    resolve({
      policies,
      schemas,
      entities,
      invariants,
      all: {
        ...policies,
        ...schemas,
        ...entities,
        ...invariants,
      },
    });
  });
}

export function verificationReducer(
  context: VerificationState,
  action: VerificationAction,
): VerificationState {
  console.log(JSON.stringify(action));

  if (action.type === "verify") {
    const newContext = {};

    // TODO: multiple versions
    Object.entries(context).forEach(([id, group]) => {
      const fileResolution = new Promise<{
        request: VerificationRequest;
        byId: VerificationFileDict;
        resolveFilenames: (report: Report) => Report;
      }>(async (resolve) => {
        const { policies, schemas, entities, invariants, all } =
          await sortFilesById(group);

        const request: VerificationRequest = {
          policyFiles: Object.keys(policies).map((id) => [
            { version: "0", id },
          ]),
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
  } else if (action.file && action.group) {
    const group: VerificationGroup = context[action.group] ?? {
      ...emptyVerificationGroup,
    };
    const newContext = { ...context };
    const newGroup = { ...group };
    const list = group[action.file.filetype];

    if (action.type === "add") {
      newGroup[action.file.filetype] = [...list, action.file];

      newGroup.byId = sortFilesById(newGroup).then(({ all }) => all);
    } else if (action.type === "remove") {
      newGroup[action.file.filetype] = [
        ...list.filter((file) => file !== action.file),
      ];

      newGroup.byId = sortFilesById(newGroup).then(({ all }) => all);

      // TODO: delete version
    } else if (action.type === "version" && action.original) {
      const original = newGroup.versioned[action.original.serverId];

      if (!original) {
        newGroup.versioned[action.original.serverId] = {
          original: action.original.file,
          versions: [],
        };
      }

      newGroup.versioned[action.original.serverId].versions.push(action.file);

      newGroup.cedar = [
        ...group.cedar.filter((policy) => policy !== action.file),
      ];
    }

    newContext[action.group] = newGroup;
    return newContext;
  }

  return context;
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
