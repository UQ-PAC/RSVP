# RSVP Web Application

[![Build Web Application](https://github.com/UQ-PAC/RSVP/actions/workflows/webapp.yaml/badge.svg)](https://github.com/UQ-PAC/RSVP/actions/workflows/webapp.yaml)

This is a [Next.js](https://nextjs.org) project bootstrapped with [`create-next-app`](https://nextjs.org/docs/app/api-reference/cli/create-next-app).

## Environment setup

This project requires Node.js, npm and pnpm to be installed. Install Node.js and npm by following
the relevant instructions in the [npm documentation](https://docs.npmjs.com/downloading-and-installing-node-js-and-npm).

Then install pnpm using npm:

```
npm install -g pnpm@latest-10
```

Before building the project for the first time you will need to install the required dependencies by running the following:

```
pnpm install
```

If you ever delete the `node_modules` directory or `pnpm-lock.yaml`, or if you encounter conflicts in `pnpm-lock.yaml` when rebasing, you will need to re-run `pnpm install`. Conflicts will be automatically resolved as part of this step.

## Local development

- Run tests:

  ```
  pnpm test
  ```

- Start the development server:

  ```
  pnpm dev
  ```

  Open [http://localhost:3000](http://localhost:3000) with your browser to see the result.
  The page auto-updates as you edit files.

  This project uses [`next/font`](https://nextjs.org/docs/app/building-your-application/optimizing/fonts) to automatically optimize and load [Geist](https://vercel.com/font), a new font family for Vercel.

## Learn More

To learn more about Next.js, take a look at the following resources:

- [Next.js Documentation](https://nextjs.org/docs) - learn about Next.js features and API.
- [Learn Next.js](https://nextjs.org/learn) - an interactive Next.js tutorial.

You can check out [the Next.js GitHub repository](https://github.com/vercel/next.js) - your feedback and contributions are welcome!

## Deploy on Vercel

The easiest way to deploy your Next.js app is to use the [Vercel Platform](https://vercel.com/new?utm_medium=default-template&filter=next.js&utm_source=create-next-app&utm_campaign=create-next-app-readme) from the creators of Next.js.

Check out our [Next.js deployment documentation](https://nextjs.org/docs/app/building-your-application/deploying) for more details.
