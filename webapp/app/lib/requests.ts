import {
  ChangeImpact,
  Report,
  UploadedFile,
  VerificationRequest,
} from "./types";
import { sortReports } from "./util";

export async function upload(file: File): Promise<UploadedFile> {
  // Create FormData object - required for sending binary file data
  const formData = new FormData();
  formData.append("file", file);

  return wrapText(
    fetch("/api/upload", {
      method: "POST",
      body: formData,
    }),
  ).then((text) => ({ serverId: text, content: download(text) }));
}

export async function remove(id: string): Promise<boolean> {
  return fetch(`/api/file/${id}`, { method: "DELETE" }).then(
    (response) => response.ok,
  );
}

export async function download(id: string): Promise<string> {
  return wrapText(fetch(`/api/file/${id}`));
}

export async function verify(request: VerificationRequest): Promise<Report[]> {
  return fetch("/api/verify", {
    headers: {
      Accept: "application/json",
      "Content-Type": "application/json",
    },
    method: "POST",
    body: JSON.stringify(request),
  })
    .then((res) => {
      if (!res.ok) {
        throw res;
      }
      return res.json();
    })
    .then(sortReports)
    .catch((err) => {
      console.error(err);
      return [];
    });
}

export async function diff(
  original: { id: string; name: string },
  updated: { id: string; name: string },
): Promise<string> {
  const params = [
    `original=${original.id}`,
    `originalName=${original.name}`,
    `updated=${updated.id}`,
    `updatedName=${updated.name}`,
  ];

  return wrapText(fetch(`/api/diff?${params.join("&")}`));
}

export async function impact(
  original: string,
  updated: string,
): Promise<ChangeImpact> {
  const params = [`original=${original}`, `updated=${updated}`];

  return fetch(`/api/impact?${params.join("&")}`)
    .then((res) => {
      if (!res.ok) {
        throw res;
      }
      return res.json();
    })
    .catch((err) => {
      console.error(err);
      return { permitted: [], forbidden: [] };
    });
}

function wrapText(res: Promise<Response>, def: string = ""): Promise<string> {
  return res
    .then((res) => {
      if (!res.ok) {
        throw res;
      }
      return res.text();
    })
    .catch((err) => {
      console.error(err);
      return def;
    });
}
