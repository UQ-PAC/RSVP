"use client";

import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import cx from "classnames";
import { useState } from "react";
import { getExpandIcon, getFileIcon } from "../../lib/fa-util";
import { VerificationFile } from "../../lib/types";
import { CodeRender } from "./CodeRender";

interface SourceFileFallbackParams {
  file: VerificationFile;
}

export function SourceFileFallback({ file }: SourceFileFallbackParams) {
  // Change icon on hover, as opposed to style
  const [toggleHover, setToggleHover] = useState(false);

  return (
    <div className={cx("source-file", "expanded")}>
      <div
        className="source-file-header"
        data-testid="source-fallback-header"
        onMouseOver={() => setToggleHover(true)}
        onMouseOut={() => setToggleHover(false)}
      >
        <FontAwesomeIcon
          className="source-file-icon"
          icon={getFileIcon(file.filetype)}
        />
        <h2 className="source-file-name">{file.filename}</h2>
        <FontAwesomeIcon
          className="source-file-toggle"
          icon={getExpandIcon(toggleHover, true)}
        />
      </div>
      <div className="source-file-content">
        <CodeRender file={file} content={Promise.resolve(" ")} reports={[]} />
      </div>
    </div>
  );
}
