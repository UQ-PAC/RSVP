import { createContext, Dispatch, useContext } from "react";
import { publish } from "../events";
import { remove, verify } from "../requests";
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
  type: "add" | "remove" | "verify" | "complete" | "update";
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

/* istanbul ignore next */
export function useVerification() {
  return useContext(VerificationContext);
}

/* istanbul ignore next */
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
    case "complete":
      return doComplete(context);
    case "add":
      return doAddGroup(context, action);
    case "remove":
      return doRemoveGroup(context, action);
    case "update":
      return doUpdateGroup(context, action);
  }
}

function doVerify(context: VerificationState): VerificationState {
  publish("verificationPending");

  const newContext = {};

  const results: Promise<Report[]>[] = [];

  Object.entries(context).forEach(([id, group]) => {
    const { error } = checkAnalysisGroup(group.files);

    if (error) {
      newContext[id] = group;
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

        const resolveFilenames = (report: Report): Report => {
          return {
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
          };
        };

        resolve({
          request,
          byId: all,
          resolveFilenames,
        });
      });

      const reports = fileResolution.then(({ request, resolveFilenames }) =>
        verify(request).then((reports) => reports.map(resolveFilenames)),
      );

      // Cache promise to trigger completion event
      results.push(reports);

      newContext[id] = {
        ...group,
        byId: fileResolution.then(({ byId }) => byId),
        reports,
        impacts: {},
        verifyPending: true,
        verifyComplete: false,
      } as AnalysisGroup;
    }
  });

  Promise.all(results)
    .then(() => publish("verificationComplete"))
    .catch(() => publish("verificationError"));

  return newContext;
}

function doComplete(context: VerificationState): VerificationState {
  const newContext = {};

  Object.entries(context).forEach(([id, group]) => {
    newContext[id] = {
      ...group,
      verifyPending: false,
      verifyComplete: true,
    } as AnalysisGroup;
  });

  return newContext;
}

function doAddGroup(
  context: VerificationState,
  action: VerificationAction,
): VerificationState {
  if (!action.group) {
    console.error(`Invalid action: ${JSON.stringify(action)}`);
    return context;
  }

  if (context[action.group]) {
    console.error(`Group "${action.group}" already exists`);
    return context;
  }

  const newContext = { ...context };

  newContext[action.group] = {
    ...emptyAnalysisGroup,
    name: action.group,
  };

  return newContext;
}

function doRemoveGroup(
  context: VerificationState,
  action: VerificationAction,
): VerificationState {
  if (!action.group) {
    console.error(`Invalid action: ${JSON.stringify(action)}`);
    return context;
  }

  const group: AnalysisGroup = context[action.group];

  if (!group) {
    console.error(`Group "${action.group}" doesn't exist.`);
    return context;
  }

  // Delete files from server
  group.files.forEach((file) => {
    file.original.resolved
      .then(({ serverId }) => serverId)
      .then((id) => remove(id));
    file.versions.forEach((version) =>
      version.resolved
        .then(({ serverId }) => serverId)
        .then((id) => remove(id)),
    );
  });

  const newContext = { ...context };

  // Remove group
  delete newContext[action.group];

  return newContext;
}

function doUpdateGroup(
  context: VerificationState,
  action: VerificationAction,
): VerificationState {
  if (!action.update) {
    console.error("Invalid action: " + JSON.stringify(action));
    return context;
  }

  const newContext = { ...context };
  newContext[action.update.name] = action.update;

  return newContext;
}
