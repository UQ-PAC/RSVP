import { useDrop } from "react-dnd";
import { useDropRef } from "./DragDropRef";

interface FileVersionDropZoneProps {
  name: string;
  onDrop: (file: { serverId: string }) => void;
}

export function FileVersionDropZone({
  name,
  onDrop,
}: FileVersionDropZoneProps) {
  // different dragitem types??
  const [{ isOver }, drop] = useDrop(() => ({
    accept: "item",
    drop: (item: { serverId: string }) => onDrop(item),
    collect: (monitor) => ({
      isOver: monitor.isOver(),
    }),
  }));

  const ref = useDropRef(drop);

  return (
    <div
      ref={ref}
      style={{
        border: `1px dashed ${isOver ? "green" : "black"}`,
        padding: "10px",
      }}
    >
      {`Drop here (${name})`}
    </div>
  );
}
