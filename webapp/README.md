# RSVP Web Application

[![Build Web Application](https://github.com/UQ-PAC/RSVP/actions/workflows/webapp.yaml/badge.svg)](https://github.com/UQ-PAC/RSVP/actions/workflows/webapp.yaml)

This is a [Next.js](https://nextjs.org) project bootstrapped with [`create-next-app`](https://nextjs.org/docs/app/api-reference/cli/create-next-app).

This project uses [`next/font`](https://nextjs.org/docs/app/building-your-application/optimizing/fonts) to automatically optimize and load [Geist](https://vercel.com/font), a new font family for Vercel.

## Environment setup

This project requires Node.js, npm, pnpm to be installed.

Install Node.js 24:

```
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.5/install.sh | bash
nvm install 24
```

More detailed instructions can be found in the [npm documentation](https://docs.npmjs.com/downloading-and-installing-node-js-and-npm).

Enable pnpm:

```
corepack enable pnpm
```

Before building the project for the first time you will need to install the required dependencies:

```
pnpm install
```

> [!NOTE]
> If you ever delete the `node_modules` directory or `pnpm-lock.yaml`, or if you encounter conflicts in `pnpm-lock.yaml` when rebasing, you will need to re-run `pnpm install`. Conflicts will be automatically resolved as part of this step.

In order to run the end-to-end tests, install Playwright headless browsers:

```
pnpm exec playwright install
```

## Local development

The React client interacts with a Spring Boot backend to execute policy verification, the server code is located in a submodule of [../rsvp](https://github.com/UQ-PAC/RSVP/tree/main/rsvp) and can be executed from that directory by running `./gradlew :webserver:bootRun`.

- Start the development server:

  ```
  pnpm dev
  ```

  Open [http://localhost:3000](http://localhost:3000) with your browser to see the result. The page auto-updates as you edit files.

- Build and start the production server:

  ```
  pnpm build
  pnpm start
  ```

- Run tests:

  ```
  pnpm test
  ```

- Run only unit tests:

  ```
  pnpm test:unit
  ```

  A detailed test coverage report can be found in `./coverage/lcov-report/index.html`.

- Run a single unit test file:

  ```
  pnpm test:unit app/page.test.ts
  ```

- Run a single unit test:

  ```
  pnpm test:unit -t "test description"
  ```

- Run only end-to-end tests:

  ```
  pnpm test:e2e
  ```

  A detailed report can be found in `./playwright-report/index.html`.

- Run a single end-to-end test file:

  ```
  pnpm test:e2e tests/page.test.ts
  ```

- Run a single end-to-end test:

  ```
  pnpm test:e2e -g "test description"
  ```

## Learn More

To learn more about Next.js, take a look at the following resources:

- [Next.js Documentation](https://nextjs.org/docs) - learn about Next.js features and API.
- [Learn Next.js](https://nextjs.org/learn) - an interactive Next.js tutorial.

You can check out [the Next.js GitHub repository](https://github.com/vercel/next.js) - your feedback and contributions are welcome!

## Deploy on Vercel

The easiest way to deploy your Next.js app is to use the [Vercel Platform](https://vercel.com/new?utm_medium=default-template&filter=next.js&utm_source=create-next-app&utm_campaign=create-next-app-readme) from the creators of Next.js.

Check out our [Next.js deployment documentation](https://nextjs.org/docs/app/building-your-application/deploying) for more details.
