import cx from "classnames";
import {
  Diff2HtmlUI,
  Diff2HtmlUIConfig,
} from "diff2html/lib/ui/js/diff2html-ui-slim.js";
import { Roboto_Mono } from "next/font/google";
import { useEffect, useRef } from "react";

import "diff2html/bundles/css/diff2html.min.css";
import "./diff.css";

interface DiffRenderProps {
  diff: Promise<string>;
}

const highlightLanguages = new Map();
highlightLanguages.set("cedar", "cedar");

const diffConfig: Diff2HtmlUIConfig = {
  drawFileList: false,
  outputFormat: "side-by-side",
  fileContentToggle: false,
  stickyFileHeaders: false,
  highlight: true,
  highlightLanguages,
};

const robotoMono = Roboto_Mono({
  subsets: ["latin"],
});

// TODO: handle identical files
export function DiffRender({ diff }: DiffRenderProps) {
  const ref = useRef<HTMLDivElement>(null);

  useEffect(() => {
    diff.then((diff) => {
      if (ref.current) {
        if (diff.length === 0) {
          ref.current.innerText = "No changes";
          ref.current.className = `${ref.current.className} source-file-empty-diff`;
        } else {
          const diffUi = new Diff2HtmlUI(ref.current, diff, diffConfig);
          diffUi.draw();
        }
      }
    });
  }, [diff]);

  return (
    <div
      ref={ref}
      className={cx("source-file-diff", robotoMono.className)}
    ></div>
  );
}
