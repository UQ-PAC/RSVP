// "use client";

// import { FileType, VerificationFile, VersionedFile } from "../../types";
// import { DndProvider } from "react-dnd";
// import { HTML5Backend } from "react-dnd-html5-backend";
// import { File } from "./File";
// import { ChangeEvent, useRef } from "react";

// interface FileListProps {
//   title: string;
//   filetype: FileType;
//   group: string;
//   files: VersionedFile[];
//   remove: (file: VerificationFile) => void;
// }

// export function FileList({
//   title,
//   filetype,
//   group,
//   files,
//   remove,
// }: FileListProps) {
//   const fileClickRef = useRef<VerificationFile>(null);
//   const fileInputRef = useRef<HTMLInputElement>(null); // Reference to hidden input element

//   const addNestedChild = (parent: VerificationFile, childId: string) => {};

//   // Handle regular file input selection
//   const handleFileInput = (e: ChangeEvent<HTMLInputElement>) => {
//     if (fileClickRef.current && e.target.files && e.target.files.length > 0) {
//       const selectedFiles = Array.from<File>(e.target.files);
//       // addFiles(selectedFiles);
//     }
//     fileClickRef.current = null;
//   };

//   // const handleDrop = (
//   //   dragged: { serverId: string },
//   //   target: VerificationFile,
//   // ) => {
//   //   console.log(
//   //     `DRAGGED:: ${JSON.stringify(dragged)} into ${target.file.name}`,
//   //   );
//   //   target.resolved
//   //     .then((resolved) =>
//   //       byId?.then((lookup) => ({ resolvedTarget: resolved, lookup })),
//   //     )
//   //     .then((result) => {
//   //       if (result) {
//   //         const { resolvedTarget, lookup } = result;
//   //         console.log(
//   //           `${lookup[dragged.serverId]?.file.name} -> ${target.file.name}`,
//   //         );
//   //         dispatch({
//   //           type: "version",
//   //           group,
//   //           file: lookup[dragged.serverId],
//   //           original: { file: target, serverId: resolvedTarget.serverId },
//   //         });
//   //       }
//   //     });
//   // };

//   // Programmatically open file selection dialog
//   const openFileDialog = (file: VerificationFile) => {
//     fileClickRef.current = file;
//     fileInputRef.current?.click();
//   };

//   const list = (
//     <div className={`file-list file-list-${filetype}`}>
//       <h5 className="file-list-title">{title}</h5>
//       <div className="file-list-contents">
//         {files.map((file, i) => (
//           <File
//             key={i}
//             group={group}
//             file={file}
//             remove={remove}
//             addChild={nested ? openFileDialog : undefined}
//           />
//         ))}
//       </div>
//       {nested && (
//         /* Hidden file input for traditional file selection */
//         <input
//           ref={fileInputRef}
//           type="file"
//           multiple // Allow multiple file selection
//           onChange={handleFileInput}
//           style={{ display: "none" }}
//           accept=".cedar, .cedarschema, .json, .invariant"
//         />
//       )}
//     </div>
//   );

//   return nested ? (
//     <DndProvider backend={HTML5Backend}>{list}</DndProvider>
//   ) : (
//     list
//   );
// }
