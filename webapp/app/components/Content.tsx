interface ContentParams {
  children?: React.ReactNode;
}

export function Content({ children }: ContentParams) {
  return <div className="app-content">{children}</div>;
}
