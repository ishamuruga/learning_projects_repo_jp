# Operational and Configuration Notes

## Build and Runtime
- Build tool: Maven (`pom.xml`)
- Java version: `17`
- Application name: `todo-api`
- Default server port: `8080`

## Dependencies
- `spring-boot-starter-web`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-validation`
- `com.h2database:h2` (runtime)
- `spring-boot-starter-test` (test scope)

## Database Configuration
- JDBC URL: `jdbc:h2:file:./data/tododb`
- Driver: `org.h2.Driver`
- Username/password: `sa` / `sa`
- Dialect: `org.hibernate.dialect.H2Dialect`
- Schema management: `spring.jpa.hibernate.ddl-auto=update`

## Observability and Developer Settings
- SQL logging enabled (`spring.jpa.show-sql=true`)
- SQL formatting enabled (`hibernate.format_sql=true`)
- H2 console enabled at `/h2-console`

## Seed Data Bootstrapping
- `DataLoader` runs at startup via `CommandLineRunner`.
- Current behavior deletes all rows and then inserts sample records.
- Includes examples in OPEN, CLOSED, and CANCELLED states.

## Environment and IDE Notes
- Workspace has Java null analysis disabled in `.vscode/settings.json`.
- `target/` contains generated build artifacts and should not be used as source-of-truth documentation input.

## Suggested Production Hardening (Future)
- Introduce profile-specific property files (`application-dev.properties`, `application-prod.properties`).
- Disable H2 console and SQL logging in production profile.
- Replace file-based H2 with managed relational database for persistent multi-user workloads.
- Restrict CORS origins to trusted front-end domains.
