import { verify } from "../../requests";
import { VerificationFile, VerificationRequest, Report } from "../../types";
import { createContext, Dispatch, useContext } from "react";

interface VerificationGroup {
  cedar: VerificationFile[]; // TODO: multiple versions
  cedarschema: VerificationFile[];
  entities: VerificationFile[];
  invariant: VerificationFile[];
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
};
export const emptyVerification: VerificationState = {};

interface VerificationAction {
  type: "add" | "remove" | "verify";
  group?: string;
  file?: VerificationFile;
}

export function verificationReducer(
  context: VerificationState,
  action: VerificationAction,
): VerificationState {
  if (action.type === "verify") {
    const newContext = {};

    const getServerId = async (file): Promise<string> =>
      (await file.resolved).serverId;

    // TODO: multiple versions
    Object.entries(context).forEach(([id, group]) => {
      const fileResolution = new Promise<{
        request: VerificationRequest;
        resolveFilenames: (report: Report) => Report;
      }>(async (resolve) => {
        const policiesById: { [id: string]: VerificationFile } = {};
        const schemasById: { [id: string]: VerificationFile } = {};
        const entitiesById: { [id: string]: VerificationFile } = {};
        const invariantsById: { [id: string]: VerificationFile } = {};

        await Promise.all([
          ...group.cedar.map((file) =>
            getServerId(file).then((id) => (policiesById[id] = file)),
          ),
          ...group.cedarschema.map((file) =>
            getServerId(file).then((id) => (schemasById[id] = file)),
          ),
          ...group.entities.map((file) =>
            getServerId(file).then((id) => (entitiesById[id] = file)),
          ),
          ...group.invariant.map((file) =>
            getServerId(file).then((id) => (invariantsById[id] = file)),
          ),
        ]);

        const request: VerificationRequest = {
          policyFiles: Object.keys(policiesById).map((id) => [
            { version: "0", id },
          ]),
          schemas: Object.keys(schemasById),
          entities: Object.keys(entitiesById),
          invariants: Object.keys(invariantsById),
        };

        const allById = {
          ...policiesById,
          ...schemasById,
          ...entitiesById,
          ...invariantsById,
        };

        const resolveFilenames = (report: Report): Report => ({
          ...report,
          primarySourceLocation: {
            ...report.primarySourceLocation,
            source: allById[report.primarySourceLocation.file],
          },
          sourceLocations: report.sourceLocations.map((sourceLoc) => ({
            ...sourceLoc,
            source: allById[sourceLoc.file],
          })),
        });

        resolve({
          request,
          resolveFilenames,
        });
      });

      const newGroup = {
        ...group,
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
    } else if (action.type === "remove") {
      newGroup[action.file.filetype] = [
        ...list.filter((file) => file !== action.file),
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
