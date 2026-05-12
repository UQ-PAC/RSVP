"use client";

import cx from "classnames";
import {
  Diff2HtmlUI,
  Diff2HtmlUIConfig,
} from "diff2html/lib/ui/js/diff2html-ui-slim.js";
import { Roboto_Mono } from "next/font/google";
import { useEffect, useRef, useState } from "react";

import "diff2html/bundles/css/diff2html.min.css";
import {
  useAnalysisGroup,
  useAnalysisGroupDispatch,
} from "../../lib/context/AnalysisGroupContext";
import { diff as getDiff, impact as getImpact } from "../../lib/requests";
import { VerificationFile } from "../../lib/types";
import { ProgressSpinner } from "../shared/ProgressSpinner";
import "./diff.css";

interface DiffRenderProps {
  originalId: string;
  updatedId: string;
  original: VerificationFile;
  updated: VerificationFile;
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

const impactConfig: Diff2HtmlUIConfig = {
  drawFileList: false,
  outputFormat: "side-by-side",
  fileContentToggle: false,
  stickyFileHeaders: false,
  highlight: false,
};

const robotoMono = Roboto_Mono({
  subsets: ["latin"],
});

export function DiffRender({
  original,
  updated,
  originalId,
  updatedId,
}: DiffRenderProps) {
  const { diffs, impacts, verifyPending, verifyCompleted } = useAnalysisGroup();
  const analysisGroupDispatch = useAnalysisGroupDispatch();

  const diffRef = useRef<HTMLDivElement>(null);
  const impactRef = useRef<HTMLDivElement>(null);

  const diffRequested = useRef(false);
  const impactRequested = useRef(false);

  const [diff, setDiff] = useState(diffs[originalId]?.[updatedId]);
  const [impact, setImpact] = useState(impacts?.[originalId]?.[updatedId]);

  const [inProgress, setInProgress] = useState(verifyPending);

  useEffect(() => {
    const existing = diffs[originalId]?.[updatedId];

    if (existing) {
      setDiff(existing);
    } else if (!diffRequested.current) {
      // Diff hasn't been requested yet
      diffRequested.current = true;
      getDiff(
        { id: originalId, name: original.file.name },
        { id: updatedId, name: updated.file.name },
      ).then((diff) => {
        setDiff(diff);
        analysisGroupDispatch({
          type: "diff",
          originalId,
          updatedId,
          diff,
        });
        diffRequested.current = false;
      });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [originalId, original, updatedId, updated]);

  useEffect(() => {
    const existing = impacts?.[originalId]?.[updatedId];

    if (existing) {
      setImpact(existing);
    } else if (!impactRequested.current && verifyCompleted) {
      impactRequested.current = true;
      setInProgress(true);

      verifyCompleted.then((result) => {
        if (result) {
          // Verification has been executed but no impact exists yet
          getImpact(originalId, updatedId).then((impact) => {
            setImpact(impact);
            setInProgress(false);
            analysisGroupDispatch({
              type: "impact",
              originalId,
              updatedId,
              diff: impact,
            });
            impactRequested.current = false;
          });
        }
      });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [verifyCompleted, originalId, updatedId]);

  // Re-render when diff is updated
  useEffect(() => {
    if (diff !== undefined && diffRef.current) {
      if (diff.length === 0) {
        diffRef.current.innerText = "No changes";
      } else {
        const diffUi = new Diff2HtmlUI(diffRef.current, diff, diffConfig);
        diffRef.current.className = `${diffRef.current.className} ${robotoMono.className}`;
        diffUi.draw();
      }
    }
  }, [diff]);

  // Re-render when impact is updated
  useEffect(() => {
    if (impact !== undefined && impactRef.current) {
      if (impact.length === 0) {
        impactRef.current.innerText = "No impact";
      } else {
        const diffUi = new Diff2HtmlUI(impactRef.current, impact, impactConfig);
        impactRef.current.className = `${impactRef.current.className} ${robotoMono.className}`;
        diffUi.draw();
      }
    }
  }, [impact]);

  const fallback = inProgress ? (
    <ProgressSpinner />
  ) : (
    <span className="diff-impact-information-message">
      Run verification to see the impact of these changes.
    </span>
  );

  return (
    <>
      <div
        ref={diffRef}
        className={cx(
          "source-file-diff",
          !diff?.length && "source-file-empty-diff",
        )}
        data-testid={diff?.length ? "diff-render" : "empty-diff-render"}
      />
      {diff && (
        <div className="diff-impact">
          {impact !== undefined ? (
            <div
              ref={impactRef}
              data-testid={
                impact.length ? "impact-render" : "empty-impact-render"
              }
              className={cx(
                "source-file-impact",
                !impact.length && "source-file-empty-impact",
              )}
            />
          ) : (
            fallback
          )}
        </div>
      )}
    </>
  );
}
