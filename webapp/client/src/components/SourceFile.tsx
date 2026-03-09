import React, { useState } from "react";

function SourceFile({ path, content }: { path: string; content: string }) {
  return (
    <div>
      <h1>{path}</h1>
      <p>{content}</p>
    </div>
  );
}

export default SourceFile;
