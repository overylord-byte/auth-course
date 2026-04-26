# Authentication Course Monorepo

## Course description

This repository contains an educational course project focused on understanding authentication and authorization mechanisms from the ground up.

## Project purpose

The goal is not only to use framework features, but to understand how authentication works internally across the full stack:

- HTTP headers
- cookies
- sessions
- tokens
- OAuth 2.0
- OpenID Connect
- PKCE
- identity providers
- frontend and backend integration

## Project structure

The monorepo is organized by applications:

- `apps/web` - frontend application used to demonstrate browser and client behavior
- `apps/server` - backend application built with Spring Boot

## Technologies

- Java
- Spring Boot
- React
- TypeScript
- HTTP Basic Authentication
- Session-based authentication
- JWT
- OAuth 2.0
- OpenID Connect
- PKCE
- Keycloak
- LDAP
- JUnit
- MockMvc

## Authentication and authorization topics covered

- Authentication vs authorization
- HTTP headers and cookies
- HTTP Basic Authentication according to RFC 7617
- Session store authentication
- JWT access tokens
- Refresh tokens
- OAuth 2.0 authorization code flow
- PKCE
- OpenID Connect
- Keycloak integration
- LDAP basics and integration

## Branch-based learning flow

Each implementation step can be reviewed in its own branch.

- Basic Authentication: TBD
- Session Authentication: TBD
- JWT: TBD
- OAuth 2.0: TBD
- OpenID Connect: TBD
- Keycloak: TBD
- LDAP: TBD

## How to run the project

1. Clone the repository.
2. Install frontend dependencies from the repository root:

```bash
pnpm install
```

3. Start the frontend:

```bash
pnpm dev:web
```

Frontend URL: `http://localhost:5173`

4. Start the Spring Boot backend:

```bash
pnpm dev:server
```

Backend URL: `http://localhost:3001`

## Development notes

- The first implementations are intentionally written without Spring Security.
- The objective is to understand the protocol and state-management mechanics before introducing framework abstractions.
- Some implementations are educational and are not production-ready.
