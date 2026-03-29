import { faPlus } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";

interface CreateContextButtonProps {
  onclick: () => void;
}

export function CreateContextButton({ onclick }: CreateContextButtonProps) {
  return (
    <button className="create-policy-context-button" onClick={onclick}>
      <FontAwesomeIcon
        className="create-policy-context-button-icon"
        icon={faPlus}
      />
      <span className="create-policy-context-button-text">
        Create policy set
      </span>
    </button>
  );
}
