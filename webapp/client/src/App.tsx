import React, { useState, useEffect } from "react";
import logo from "./logo.svg";
import "./App.css";

// Import React FilePond
import { FilePond } from "react-filepond";
import { FilePondFile } from "filepond";

// Import FilePond styles
import "filepond/dist/filepond.min.css";

function App() {
  const [data, setData] = React.useState(null);
  const [files, setFiles] = useState([] as any[]);

  React.useEffect(() => {
    fetch("/api/hello")
      .then((res) => res.json())
      .then((data) => setData(data.message));
  }, []);

  return (
    <div className="App">
      <h1>Policy Verification</h1>
      <FilePond
        files={files}
        onupdatefiles={setFiles}
        allowMultiple={true}
        maxFiles={20}
        server="/api"
        name="files" /* sets the file input name, it's filepond by default */
        labelIdle='Drag & Drop your files or <span class="filepond--label-action">Browse</span>'
      />
    </div>
  );
}

export default App;
