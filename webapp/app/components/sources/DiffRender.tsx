import { VerificationFile } from "@/app/types";
import { useEffect, useRef } from "react";

import {
  Diff2HtmlUI,
  Diff2HtmlUIConfig,
} from "diff2html/lib/ui/js/diff2html-ui-slim.js";

import "diff2html/bundles/css/diff2html.min.css";
import "./diff.css";

interface DiffRenderProps {
  left: VerificationFile;
  right: VerificationFile;
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

// TODO: handle identical files
export function DiffRender({ left, right, diff }: DiffRenderProps) {
  const ref = useRef<HTMLDivElement>(null);

  useEffect(() => {
    diff.then((diff) => {
      console.log(diff);

      //   const diffjson = diff2html.parse(diff);
      //   const html = diff2html.html(diffjson, {
      //     drawFileList: false,
      //     outputFormat: "side-by-side",
      //     // matching: "lines",
      //     highlightLanguages: "cedar",
      //   });

      if (ref.current) {
        const diffUi = new Diff2HtmlUI(ref.current, diff, diffConfig);
        diffUi.draw();
      }
    });
  }, [diff]);

  return <div ref={ref} className="source-file-diff"></div>;
}
