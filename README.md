# React TypeScript Monorepo

This repository is a clean monorepo foundation for a future authentication course project.

## Structure

- `apps/web`: React + TypeScript + Tailwind CSS frontend (Vite)
- `apps/server`: backend placeholder for future implementation

## Tech stack

- `pnpm` workspaces
- `Turborepo` for running tasks from the root
- `React` + `TypeScript` + `Tailwind CSS` in the frontend
- `tsc` for type checking

## Getting started

1. Install dependencies:

```bash
pnpm install
```

2. Start frontend development server:

```bash
pnpm dev
```

3. Open the app:

```text
http://localhost:5173
```

## Useful scripts

From the repository root:

- `pnpm dev` - run frontend in development mode
- `pnpm build` - build frontend
- `pnpm typecheck` - run TypeScript type checking (`tsc --noEmit`)
- `pnpm lint` - run frontend linting

You can also use explicit frontend scripts:

- `pnpm dev:web`
- `pnpm build:web`
- `pnpm typecheck:web`
- `pnpm lint:web`
