import { useVerificationDispatch } from "./providers/VerificationContext";
import { VerifyButton } from "./VerifyButton";
import { Lexend_Giga } from "next/font/google";
interface HeaderParams {
  heading: string;
  subheading?: string;
}

const lexendGiga = Lexend_Giga({
  subsets: ["latin"],
});

export function Header({ heading, subheading }: HeaderParams) {
  return (
    <div className="app-header">
      <div className="header-left-align">
        <h1 className={`title large-text bold-text ${lexendGiga.className}`}>
          {heading}
        </h1>
        {subheading && <h2 className="subtitle med-text">{subheading}</h2>}
      </div>
      <div className="header-right-align">
        <VerifyButton />
      </div>
    </div>
  );
}
