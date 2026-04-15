import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  UploadedFile as ResolvedFile,
  VerificationFile,
  VersionedFile,
} from "../../types";
import { faCodeCompare, faXmark } from "@fortawesome/free-solid-svg-icons";
import { FileVersionDropZone } from "./FileVersionDropZone";
import { DraggableFile } from "./DraggableFile";
import { JSX, useEffect, useState } from "react";
import {
  useVerification,
  useVerificationDispatch,
} from "../providers/VerificationContext";

interface UploadedFileProps {
  file: VerificationFile;
  group: string;
  remove: (file: VerificationFile) => void;
  move?: (
    file: VerificationFile,
    target?: VerificationFile,
    index?: number,
  ) => void;
  children?: JSX.Element[];
  addChild?: (file: VerificationFile) => void;
}

export function UploadedFile({
  file,
  group,
  remove,
  move,
  children,
  addChild,
}: UploadedFileProps) {
  // const [resolved, setResolved] = useState<ResolvedFile>();
  // const [versions, setVersions] = useState<VerificationFile[]>([]);

  // const context = useVerification();
  // const dispatch = useVerificationDispatch();

  // const fileGroup = context[group];

  // const byId = fileGroup?.byId;
  // const versionedFiles = fileGroup?.versioned;

  // useEffect(() => {
  //   if (addChild) {
  //     file.resolved.then((resolved) => {
  //       setResolved(resolved);
  //       setVersions(versionedFiles?.[resolved.serverId]?.versions ?? []);
  //     });
  //   }
  // }, [file, addChild, versionedFiles, context]);

  // const handleDrop = (
  //   dragged: { serverId: string },
  //   target: VerificationFile,
  // ) => {
  //   console.log(
  //     `DRAGGED:: ${JSON.stringify(dragged)} into ${target.file.name}`,
  //   );
  //   target.resolved
  //     .then((resolved) =>
  //       byId?.then((lookup) => ({ resolvedTarget: resolved, lookup })),
  //     )
  //     .then((result) => {
  //       if (result) {
  //         const { resolvedTarget, lookup } = result;
  //         console.log(
  //           `${lookup[dragged.serverId]?.file.name} -> ${target.file.name}`,
  //         );
  //         dispatch({
  //           type: "version",
  //           group,
  //           file: lookup[dragged.serverId],
  //           original: { file: target, serverId: resolvedTarget.serverId },
  //         });
  //       }
  //     });
  // };

  // const element = move ? (
  //   <DraggableFile file={file} resolved={resolved} remove={remove} />
  // ) : (
  //   <span className="uploaded-file">
  //     <span className="uploaded-file-name">{file.file.name}</span>
  //     {addChild && (
  //       <FontAwesomeIcon
  //         className="uploaded-file-version-icon"
  //         icon={faCodeCompare}
  //         onClick={() => addChild(file)}
  //       />
  //     )}
  //     <FontAwesomeIcon
  //       className="uploaded-file-delete-icon"
  //       icon={faXmark}
  //       onClick={() => remove(file)}
  //     />
  //   </span>
  // );

  return (
    <div className="uploaded-file-container">
      <span className="uploaded-file">
        <span className="uploaded-file-name">{file.file.name}</span>
        {addChild && (
          <FontAwesomeIcon
            className="uploaded-file-version-icon"
            icon={faCodeCompare}
            onClick={() => addChild(file)}
          />
        )}
        <FontAwesomeIcon
          className="uploaded-file-delete-icon"
          icon={faXmark}
          onClick={() => remove(file)}
        />
      </span>
      <div className="uploaded-file-versions">{children}</div>
      {/* {addChild && (
        <FileVersionDropZone
          name={file.file.name}
          onDrop={(dragged) => handleDrop(dragged, file)}
        />
      )} */}
    </div>
  );
}
