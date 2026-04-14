import cx from "classnames";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { UploadedFile, VerificationFile } from "../../types";
import { faXmark } from "@fortawesome/free-solid-svg-icons";
import { useDrag } from "react-dnd";
import { useDragRef } from "./DragDropRef";

interface DraggableFileProps {
  file: VerificationFile;
  resolved: UploadedFile;
  remove: (file: VerificationFile) => void;
}

export function DraggableFile({ file, resolved, remove }: DraggableFileProps) {
  const [{ isDragging }, drag] = useDrag(() => ({
    type: "item",
    item: { serverId: resolved.serverId },
    collect: (monitor) => ({
      isDragging: monitor.isDragging(),
    }),
  }));

  const ref = useDragRef(drag);

  const className = cx(
    "uploaded-file",
    "uploaded-file-draggable",
    isDragging && "dragging",
  );

  return (
    <span ref={ref} className={className}>
      <span className="uploaded-file-name">{file.file.name}</span>
      <FontAwesomeIcon
        className="uploaded-file-delete-icon"
        icon={faXmark}
        onClick={() => remove(file)}
      />
    </span>
  );
}
