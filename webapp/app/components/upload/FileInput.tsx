"use client";

import { useRef, useState } from "react";

interface FileInputProps {
  addFiles: (files: File[]) => void;
}

export function FileInput({ addFiles }: FileInputProps) {
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
    if (e.dataTransfer.items && e.dataTransfer.items.length > 0) {
      setIsDragging(true);
    }
  };

  // Called when files are dragged out of the drop zone
  const handleDragLeave = (e) => {
    e.preventDefault();
    dragCounter.current--; // Decrement counter

    // Only set dragging to false when all drag events are complete
    if (dragCounter.current === 0) {
      setIsDragging(false);
    }
  };

  // Called continuously while dragging over the drop zone
  const handleDragOver = (e) => {
    e.preventDefault(); // Prevent default behavior (opening file)
  };

  // Called when files are dropped
  const handleDrop = (e) => {
    e.preventDefault();
    setIsDragging(false);
    dragCounter.current = 0;

    // Process dropped files
    if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
      const droppedFiles = Array.from<File>(e.dataTransfer.files); // Convert FileList to Array
      addFiles(droppedFiles);
    }
  };

  // Handle regular file input selection
  const handleFileInput = (e) => {
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
        className={`drop-zone ${isDragging ? "dragging" : ""}`}
        onDragStart={handleDragStart}
        onDragEnter={handleDragEnter}
        onDragLeave={handleDragLeave}
        onDragOver={handleDragOver}
        onDrop={handleDrop}
        onClick={openFileDialog}
      >
        {/* Hidden file input for traditional file selection */}
        <input
          ref={fileInputRef}
          type="file"
          multiple // Allow multiple file selection
          onChange={handleFileInput}
          style={{ display: "none" }}
          accept=".cedar, .cedarschema, .json, .invariant"
        />

        <div className="drop-message">
          {isDragging ? (
            <p>Drop files here</p>
          ) : (
            <div>
              <p>Drag and drop files or click to browse</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
