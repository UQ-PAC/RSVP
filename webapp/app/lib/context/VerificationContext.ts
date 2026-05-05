import { createContext, Dispatch, useContext } from "react";
import { verify } from "../requests";
import {
  AnalysisGroup,
  Report,
  VerificationFile,
  VerificationFileDict,
  VerificationRequest,
} from "../types";
import { checkAnalysisGroup } from "../util";
import { emptyAnalysisGroup, sortFilesById } from "./AnalysisGroupContext";

export interface VerificationState {
  [id: string]: AnalysisGroup;
}

export const emptyVerification: VerificationState = {};

export interface VerificationAction {
  type: "add" | "remove" | "pre-verify" | "verify" | "update";
  group?: string;
  file?: VerificationFile;
  index?: number;
  original?: VerificationFile;
  update?: AnalysisGroup;
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
    case "pre-verify":
      return doPreVerify(context);
    case "verify":
      return doVerify(context);
    case "add":
      return doAdd(context, action);
    case "remove":
      return doRemove(context, action);
    case "update":
      return updateGroup(context, action);
  }
}

function doPreVerify(context: VerificationState): VerificationState {
  const newContext = {};

  Object.entries(context).forEach(([id, group]) => {
    newContext[id] = {
      ...group,
      verifyRequested: true,
      verifyPending: true,
    } as AnalysisGroup;
  });

  return newContext;
}

function doVerify(context: VerificationState): VerificationState {
  const newContext = {};

  Object.entries(context).forEach(([id, group]) => {
    const { error } = checkAnalysisGroup(group.files);

    if (error) {
      newContext[id] = {
        ...group,
      } as AnalysisGroup;
    } else {
      const fileResolution = new Promise<{
        request: VerificationRequest;
        byId: VerificationFileDict;
        resolveFilenames: (report: Report) => Report;
      }>(async (resolve) => {
        const { policies, schemas, entities, invariants, versions, all } =
          await sortFilesById(group);

        const request: VerificationRequest = {
          policies: Object.keys(policies).map((id) => {
            return [id, ...versions[id]];
          }),
          schemas: Object.keys(schemas),
          entities: Object.keys(entities),
          invariants: Object.keys(invariants),
        };

        const resolveFilenames = (report: Report): Report => ({
          ...report,
          sourceLocations: report.sourceLocations.map(
            ({ message, location }) => ({
              message,
              location: {
                ...location,
                source: all[location.file],
              },
            }),
          ),
        });

        resolve({
          request,
          byId: all,
          resolveFilenames,
        });
      });

      const reports = fileResolution.then(({ request, resolveFilenames }) =>
        verify(request).then((reports) => reports.map(resolveFilenames)),
      );

      newContext[id] = {
        ...group,
        byId: fileResolution.then(({ byId }) => byId),
        reports,
        impacts: {},
        verifyCompleted: reports.then(() => true).catch(() => false),
      } as AnalysisGroup;
    }
  });

  return newContext;
}

function doAdd(
  context: VerificationState,
  action: VerificationAction,
): VerificationState {
  if (!action.group) {
    console.error(`Invalid action: ${JSON.stringify(action)}`);
    return context;
  }

  // Copy current context
  const group: AnalysisGroup = context[action.group] ?? {
    ...emptyAnalysisGroup,
    name: action.group,
  };
  const newContext = { ...context };
  const newGroup = { ...group };

  newContext[action.group] = newGroup;
  return newContext;
}

function doRemove(
  context: VerificationState,
  action: VerificationAction,
): VerificationState {
  if (!action.group) {
    console.error(
      `Invalid action: { file: ${action.file}, group: ${action.group} }`,
    );
    return context;
  }

  const group: AnalysisGroup = context[action.group];

  if (!group) {
    console.error(`Group "${action.group} doesn't exist.`);
    return context;
  }

  const newContext = { ...context };

  // Remove group
  delete newContext[action.group];

  return newContext;
}

function updateGroup(
  context: VerificationState,
  action: VerificationAction,
): VerificationState {
  if (!action.group || !action.update) {
    return context;
  }

  const newContext = { ...context };
  newContext[action.group] = action.update;

  return newContext;
}
