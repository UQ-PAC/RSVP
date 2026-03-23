import { faFileShield } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { Lexend_Deca } from "next/font/google";

const lexendDeca = Lexend_Deca({
  subsets: ["latin"],
});

interface VerifyButtonParams {
  onclick: () => void;
}

export function VerifyButton({ onclick }: VerifyButtonParams) {
  return (
    <button className="verify-button" onClick={onclick}>
      <FontAwesomeIcon className="verify-button-icon" icon={faFileShield} />
      <span className={`verify-button-text ${lexendDeca.className}`}>
        Verify
      </span>
    </button>
  );
}
