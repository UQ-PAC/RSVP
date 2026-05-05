import { faSpinner } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";

import "./spinner.css";

interface ProgressSpinnerProps {
  text?: string;
}

export function ProgressSpinner({ text }: ProgressSpinnerProps) {
  return (
    <div className="progress-spinner">
      <FontAwesomeIcon className="progress-spinner-icon" icon={faSpinner} />
      {!!text && <p className="progress-spinner-text">{text}</p>}
    </div>
  );
}
