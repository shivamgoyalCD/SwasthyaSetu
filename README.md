# SwasthyaSetu

## Setup
- Java 21
- Maven 3.9+
- Docker (for infra)
- Node.js (for frontend, when added)

## Run Infra
See `infra/README.md`.

## Run Backend
Example:

```powershell
mvn -pl services/api-gateway spring-boot:run
```

## Run Frontend
TBD.

## Docs
- `infra/README.md`
- `infra/verify.md`
- `services/FLYWAY_STRATEGY.md`
