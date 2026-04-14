import { UploadedFile, VerificationRequest, Report } from "./types";
import { sortReports } from "./util";

export async function upload(file: File): Promise<UploadedFile> {
  // Create FormData object - required for sending binary file data
  const formData = new FormData();
  formData.append("file", file); // Add file with 'file' key

  return fetch("/api/upload", {
    method: "POST",
    body: formData, // Send FormData directly, not JSON
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
