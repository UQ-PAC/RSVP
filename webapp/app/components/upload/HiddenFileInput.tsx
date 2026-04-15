"use client";

import { ChangeEvent, Ref } from "react";

interface HiddenFileInputProps {
  accept: string;
  ref: Ref<HTMLInputElement>;
  handleFileInput: (e: ChangeEvent<HTMLInputElement>) => void;
}

export function HiddenFileInput({
  accept,
  ref,
  handleFileInput,
}: HiddenFileInputProps) {
  // Hidden file input for traditional file selection
  return (
    <input
      ref={ref}
      type="file"
      multiple
      onChange={handleFileInput}
      style={{ display: "none" }}
      accept={accept}
    />
  );
}
