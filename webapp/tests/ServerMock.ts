import fs from "fs";
import path from "path";
import { Request, Route } from "playwright/test";

export class ServerMock {
  private files: { [hash: string]: string };

  constructor() {
    this.files = {};
  }

  public upload(): (route: Route, request: Request) => Promise<void> {
    return (route, request) => {
      const name = request
        .postDataBuffer()
        ?.toString()
        .split("\r\n")
        .at(1)
        ?.split(";")
        .at(-1)
        ?.split('"')
        .at(1);
      const content = request.postDataBuffer()?.toString().split("\r\n").at(-3);
      if (!name || !content) {
        return route.fulfill({ status: 400 });
      }

      this.files[name] = content;
      return route.fulfill({ body: name });
    };
  }

  public file(): (route: Route, request: Request) => Promise<void> {
    return (route, request) => {
      const hash = request.url().split("/").at(-1);
      if (!hash) {
        return route.fulfill({ status: 400 });
      }
      if (!this.files[hash]) {
        return route.fulfill({ status: 404 });
      }

      if (request.method() === "GET") {
        return route.fulfill({ body: this.files[hash] });
      } else if (request.method() === "DELETE") {
        return route.fulfill();
      } else {
        return route.fulfill({ status: 400 });
      }
    };
  }

  public verify(): (route: Route, request: Request) => Promise<void> {
    return async (route) => {
      return new Promise((resolve, reject) => {
        fs.readFile(
          path.join(__dirname, "resources", "reports.json"),
          { encoding: "utf-8" },
          (err, data) => {
            if (err) {
              reject(err);
            }

            resolve(JSON.parse(data));
          },
        );
      }).then((json) => route.fulfill({ json }));
    };
  }
}
