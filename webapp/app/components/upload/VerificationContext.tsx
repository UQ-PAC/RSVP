import { FileInput } from "./FileInput";
import { VerificationFileList } from "./VerificationFileList";

interface VerificationContextProps {
  name: string;
  addFiles: (files: File[]) => void;
}

export function VerificationContext({
  name,
  addFiles,
}: VerificationContextProps) {
  return (
    <div className="verification-context">
      <h4 className="verification-context-title">{name}</h4>
      <VerificationFileList group={name} />
      <FileInput addFiles={addFiles} />
    </div>
  );
}
