import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { FileType, VerificationFile } from "../../types";
import { faXmark } from "@fortawesome/free-solid-svg-icons";

interface VerificationContextProps {
  title: string;
  filetype: FileType;
  files: VerificationFile[];
  remove: (file: VerificationFile) => void;
}

export function FileGroup({
  title,
  filetype,
  files,
  remove,
}: VerificationContextProps) {
  return (
    <div
      className={`verification-file-group verification-file-group-${filetype}`}
    >
      <h5 className="verification-file-group-title">{title}</h5>
      <div className="verification-file-group-contents">
        {files.map((file, i) => (
          <span key={i} className="verification-file">
            <span className="verification-file-name">{file.file.name}</span>
            <FontAwesomeIcon
              className="verification-file-delete-icon"
              icon={faXmark}
              onClick={() => remove(file)}
            />
          </span>
        ))}
      </div>
    </div>
  );
}
