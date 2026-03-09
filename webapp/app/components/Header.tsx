interface HeaderParams {
  children: React.ReactNode;
}

export function Header({ children }: HeaderParams) {
  return <h1 className="app-header">{children}</h1>;
}
