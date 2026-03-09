interface VerifyButtonParams {
  onclick: () => void;
}

export function VerifyButton({ onclick }: VerifyButtonParams) {
  return <button onClick={onclick}>Verify</button>;
}
