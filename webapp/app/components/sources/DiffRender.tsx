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
import {
  ExpansionState,
  useFocusDispatch,
} from "../../lib/context/FocusContext";
import { useSelectionDispatch } from "../../lib/context/SelectionContext";
import { diff as getDiff, impact as getImpact } from "../../lib/requests";
import { ChangeImpact, SourceLoc, VerificationFile } from "../../lib/types";
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
  const selectionDispatch = useSelectionDispatch();
  const focusDispatch = useFocusDispatch();

  const diffRef = useRef<HTMLDivElement>(null);

  const [diff, setDiff] = useState<string>();
  const [impact, setImpact] = useState<ChangeImpact>();

  const [inProgress, setInProgress] = useState(verifyPending);

  useEffect(() => {
    const existing = diffs[originalId]?.[updatedId];

    if (existing) {
      existing.then((existingDiff) => setDiff(existingDiff));
    } else {
      // Diff hasn't been requested yet
      const result = getDiff(
        { id: originalId, name: original.file.name },
        { id: updatedId, name: updated.file.name },
      );

      analysisGroupDispatch({
        type: "diff",
        originalId,
        updatedId,
        diff: result,
      });

      result.then((newDiff) => setDiff(newDiff));
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [originalId, original, updatedId, updated]);

  useEffect(() => {
    const existing = impacts?.[originalId]?.[updatedId];

    if (existing) {
      existing.then((existingImpact) => setImpact(existingImpact));
    } else if (verifyCompleted) {
      // Verification has been executed but no impact exists yet
      verifyCompleted.then((result) => {
        if (result) {
          setInProgress(true);

          const result = getImpact(originalId, updatedId);

          analysisGroupDispatch({
            type: "impact",
            originalId,
            updatedId,
            impact: result,
          });

          result.then((newImpact) => {
            setImpact(newImpact);
            setInProgress(false);
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

  const clickRequest = (loc: SourceLoc) => {
    focusDispatch({
      type: "focus",
      target: "source-file",
      focus: { key: loc.file, value: ExpansionState.Expanded },
    });
    selectionDispatch({
      scroll: "source",
      loc: loc.file + ":" + loc.startLoc?.line,
      highlighted: {
        file: loc.file,
        start: loc.startLoc?.line ?? 0,
        end: loc.endLoc?.line ?? 0,
      },
    });
  };

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
      {diff && impact && (
        <div className="diff-impact">
          {!impact.permitted.length && !impact.forbidden.length && (
            <div
              data-testid="empty-impact-render"
              className="source-file-impact source-file-empty-impact"
            >
              No impact
            </div>
          )}
          {(!!impact.permitted.length || !!impact.forbidden.length) && (
            <div
              data-testid="impact-render"
              className={`source-file-impact ${robotoMono.className}`}
            >
              <div className="impact-render-changes impact-render-changes-forbidden">
                {impact.forbidden.map((forbid, i) => (
                  <div
                    key={i}
                    className="impact-render-change impact-render-change-forbidden"
                    onClick={() => clickRequest(forbid.locations[0])}
                  >
                    <span className="impact-render-summary impact-render-forbidden-summary">
                      {forbid.summary}
                    </span>
                  </div>
                ))}
              </div>
              <div className="impact-render-changes impact-render-changes-permitted">
                {impact.permitted.map((permit, i) => (
                  <div
                    key={i}
                    className="impact-render-change impact-render-change-permitted"
                    onClick={() => clickRequest(permit.locations[0])}
                  >
                    <span className="impact-render-summary impact-render-permitted-summary">
                      {permit.summary}
                    </span>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      )}
      {diff && !impact && (
        <div className="diff-impact">
          {verifyPending || inProgress ? (
            <ProgressSpinner />
          ) : (
            <span className="diff-impact-information-message">
              Run verification to see the impact of these changes.
            </span>
          )}
        </div>
      )}
    </>
  );
}
