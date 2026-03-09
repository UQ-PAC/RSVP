# RSVP Web Application

[![Build Web Application](https://github.com/UQ-PAC/RSVP/actions/workflows/webapp.yaml/badge.svg)](https://github.com/UQ-PAC/RSVP/actions/workflows/webapp.yaml)

## Local development

This project requires Node.js, npm and pnpm to be installed. Install Node.js and npm by following
the relevant instructions in the [npm documentation](https://docs.npmjs.com/downloading-and-installing-node-js-and-npm).

Then install pnpm using npm:

```
npm install -g pnpm@latest-10
```

### Node.js server

The server is configured in `package.json` and `tsconfig.json`, all server source files are located
in `./src`.

- Install project dependencies:
  ```
  pnpm install
  ```
- Run tests:
  ```
  pnpm test:server
  ```
- Start server:
  ```
  pnpm start:server
  ```

### React.js client

The client is configured in `client/package.json` and `client/tsconfig.json`, all client source files
are located in `client/src`.

- Install project dependencies:
  ```
  cd client
  pnpm install
  ```
- Run tests:
  ```
  cd client
  pnpm test
  ```
  Or from root directory:
  ```
  pnpm test:client
  ```
- Start client:
  ```
  cd client
  pnpm start
  ```
  Or from root directory:
  ```
  pnpm start:client
  ```
  This will start the React development server, which automatically rebuilds on file changes.
