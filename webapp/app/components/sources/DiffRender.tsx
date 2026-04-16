import * as diff2html from "diff2html";

import { VerificationFile } from "@/app/types";
import { useEffect, useRef } from "react";

import "diff2html/bundles/css/diff2html.min.css";
import { diffHighlight } from "diff2html/lib/render-utils";

interface DiffRenderProps {
  left: VerificationFile;
  right: VerificationFile;
  diff: Promise<string>;
}

export function DiffRender({ left, right, diff }: DiffRenderProps) {
  const ref = useRef<HTMLDivElement>(null);

  useEffect(() => {
    diff.then((diff) => {
      console.log(diff);

      const diffjson = diff2html.parse(diff);
      const html = diff2html.html(diffjson, {
        drawFileList: false,
        outputFormat: "side-by-side",
        // matching: "lines",
        highlightLanguages: "cedar",
      });

      if (ref.current) {
        ref.current.innerHTML = html;
      } else {
        console.log(html);
      }
    });
  }, [diff]);

  return <div ref={ref} className="source-file-diff"></div>;
}
