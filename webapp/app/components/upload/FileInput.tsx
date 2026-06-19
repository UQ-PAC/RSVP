"use client";

import cx from "classnames";
import { useRef, useState } from "react";
import { HiddenFileInput } from "./HiddenFileInput";

interface FileInputProps {
  error?: boolean;
  addFiles: (files: File[]) => void;
}

export function FileInput({ error, addFiles }: FileInputProps) {
  const [isDragging, setIsDragging] = useState(false); // Track drag state
  const fileInputRef = useRef<HTMLInputElement>(null); // Reference to hidden input element
  const dragCounter = useRef<number>(0); // Counter to handle nested drag events

  const handleDragStart = (e) => {
    e.dataTransfer.clearData();
    e.dataTransfer.setData("text/plain", e.target.dataset.item);
  };

  // Called when files are dragged into the drop zone
  const handleDragEnter = (e) => {
    e.preventDefault(); // Prevent default browser behavior
    dragCounter.current++; // Increment counter for nested elements

    // Check if dragged items contain files
    /* istanbul ignore else */
    if (e.dataTransfer.items && e.dataTransfer.items.length > 0) {
      setIsDragging(true);
    }
  };

  // Called when files are dragged out of the drop zone
  const handleDragLeave = (e) => {
    e.preventDefault();
    dragCounter.current--; // Decrement counter

    // Only set dragging to false when all drag events are complete
    /* istanbul ignore else */
    if (dragCounter.current === 0) {
      setIsDragging(false);
    }
  };

  // Called continuously while dragging over the drop zone
  const handleDragOver = (e) => {
    /* istanbul ignore next */
    e.preventDefault(); // Prevent default behavior (opening file)
  };

  // Called when files are dropped
  const handleDrop = (e) => {
    e.preventDefault();
    setIsDragging(false);
    dragCounter.current = 0;

    // Process dropped files
    /* istanbul ignore else */
    if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
      const droppedFiles = Array.from<File>(e.dataTransfer.files); // Convert FileList to Array
      addFiles(droppedFiles);
    }
  };

  // Handle regular file input selection
  const handleFileInput = (e) => {
    /* istanbul ignore else */
    if (e.target.files && e.target.files.length > 0) {
      const selectedFiles = Array.from<File>(e.target.files);
      addFiles(selectedFiles);
    }
  };

  // Programmatically open file selection dialog
  const openFileDialog = () => {
    fileInputRef.current?.click();
  };

  return (
    <div className="file-input">
      <div
        className={cx("drop-zone", isDragging && "dragging", error && "error")}
        onDragStart={handleDragStart}
        onDragEnter={handleDragEnter}
        onDragLeave={handleDragLeave}
        onDragOver={handleDragOver}
        onDrop={handleDrop}
        onClick={openFileDialog}
        data-testid="drop-zone"
      >
        <HiddenFileInput
          accept=".cedar, .cedarschema, .json, .invariant"
          ref={fileInputRef}
          handleFileInput={handleFileInput}
        />

        <div className="drop-message">
          {isDragging ? (
            <p>Drop files here</p>
          ) : (
            <p>Drag and drop files or click to browse</p>
          )}
        </div>
      </div>
    </div>
  );
}
