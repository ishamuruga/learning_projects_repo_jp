# API Sequence Diagrams

This folder contains PlantUML sequence diagrams for each Todo API endpoint.

## Endpoint Mapping

| Endpoint | Diagram File |
|---|---|
| `POST /api/todos` | `create-todo-sequence.puml` |
| `GET /api/todos` | `list-todos-sequence.puml` |
| `GET /api/todos/{id}` | `get-todo-by-id-sequence.puml` |
| `PATCH /api/todos/{id}/close` | `close-todo-sequence.puml` |
| `PATCH /api/todos/{id}/cancel` | `cancel-todo-sequence.puml` |

## Render Options

- VS Code extension: install a PlantUML preview extension and open any `.puml` file.
- PlantUML CLI (if available):

```bash
plantuml docs/diagrams/api/*.puml
```

Generated images will typically be created alongside each `.puml` file.
