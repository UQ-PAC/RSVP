import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faCirclePlus } from "@fortawesome/free-solid-svg-icons";

interface CreateContextButtonProps {
  onclick: () => void;
}

export function CreateContextButton({ onclick }: CreateContextButtonProps) {
  return (
    <button className="create-policy-context-button" onClick={onclick}>
      <FontAwesomeIcon
        className="create-policy-context-icon"
        icon={faCirclePlus}
      />
      <span className="create-policy-context-button-text">
        Create policy set
      </span>
    </button>
  );
}
