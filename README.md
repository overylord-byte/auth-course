# Auth Course

Практический курс authentication/authorization: сначала ручная реализация, затем тот же стандарт через framework/library.

- [План курса](PLAN.md)
- [Текущее состояние](PROGRESS.md)
- [Правила работы](AGENTS.md)

Код: apps/server — Spring Boot; apps/web — React/TypeScript; bruno — HTTP-сценарии; demos — демонстрации.

Локальный запуск из корня:
```sh
pnpm install
pnpm dev:web
pnpm dev:server
```

Последние документированные адреса: frontend http://localhost:5173, backend http://localhost:3001; при изменении конфигурации сверяй фактический вывод запуска. Ручные реализации предназначены для обучения.
