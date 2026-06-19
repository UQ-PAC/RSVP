import { act, render, screen, waitFor } from "@testing-library/react";
import { SourceFile } from "./SourceFile";

var expansionCallback = jest.fn();

jest.mock("./SingleSourceFile", () => ({
  SingleSourceFile: jest.fn(
    ({ file, uploaded, reports, setExpansionCallback }) => {
      setExpansionCallback(expansionCallback);
      return (
        <div
          data-testid="single-source-file"
          data-file={file.filename}
          data-uploaded={uploaded.serverId}
          data-reports={JSON.stringify(reports)}
        />
      );
    },
  ),
}));

jest.mock("./SourceFileFallback", () => ({
  SourceFileFallback: jest.fn(({ file }) => {
    return <div data-testid="source-file-fallback" data-file={file.filename} />;
  }),
}));

jest.mock("./VersionedSourceFile", () => ({
  VersionedSourceFile: jest.fn(({ files, reports, setExpansionCallback }) => {
    setExpansionCallback(expansionCallback);
    return (
      <div
        data-testid="versioned-source-file"
        data-reports={JSON.stringify(reports)}
      >
        {files.map(({ file, resolved }, i) => (
          <div
            key={i}
            data-testid="source-file-version"
            data-file={file.filename}
            data-uploaded={resolved.serverId}
          />
        ))}
      </div>
    );
  }),
}));

test("renders", async () => {
  const single = {
    original: {
      filename: "single-file.txt",
      resolved: Promise.resolve({
        serverId: "123",
      }),
    },
    versions: [],
  } as any;

  const reports = Promise.resolve<any>(["report!", "another report"]);

  let asFragment, rerender;

  // Renders single file
  act(() => {
    ({ asFragment, rerender } = render(
      <SourceFile
        source={single}
        reports={reports}
        setExpansionCallback={jest.fn()}
      />,
    ));
  });

  // Renders fallback while loading
  expect(asFragment()).toMatchSnapshot();

  await waitFor(() => {
    expect(screen.getByTestId("single-source-file")).toBeInTheDocument();
  });

  expect(asFragment()).toMatchSnapshot();

  // Renders versioned file
  act(() => {
    rerender(
      <SourceFile
        source={
          {
            original: {
              filename: "versioned-file.txt",
              resolved: Promise.resolve({
                serverId: "123",
              }),
            },
            versions: [
              {
                filename: "versioned-file.txt",
                resolved: Promise.resolve({
                  serverId: "456",
                }),
              },
              {
                filename: "versioned-file.txt",
                resolved: Promise.resolve({
                  serverId: "789",
                }),
              },
            ],
          } as any
        }
        reports={reports}
        setExpansionCallback={jest.fn()}
      />,
    );
  });

  await waitFor(() => {
    expect(screen.getByTestId("versioned-source-file")).toBeInTheDocument();
  });

  expect(asFragment()).toMatchSnapshot();
});
