import { UploadedFile, VerificationRequest, Report } from "./types";
import { sortReports } from "./util";

export async function upload(file: File): Promise<UploadedFile> {
  // Create FormData object - required for sending binary file data
  const formData = new FormData();
  formData.append("file", file);

  return fetch("/api/upload", {
    method: "POST",
    body: formData,
  })
    .then((res) => res.text())
    .then((text) => ({ serverId: text, content: download(text) }));
}

export async function remove(id: string): Promise<boolean> {
  return fetch(`/api/file/${id}`, { method: "DELETE" }).then(
    (response) => response.ok,
  );
}

export async function download(id: string): Promise<string> {
  return fetch(`/api/file/${id}`).then((res) => res.text());
}

// TODO: check if response is OK
export async function verify(request: VerificationRequest): Promise<Report[]> {
  return fetch("/api/verify", {
    headers: {
      Accept: "application/json",
      "Content-Type": "application/json",
    },
    method: "POST",
    body: JSON.stringify(request),
  })
    .then((res) => res.json())
    .then(sortReports);
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

  return fetch(`/api/diff?&${params.join("&")}`).then((res) => res.text());
}
