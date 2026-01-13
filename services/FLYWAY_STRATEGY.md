# Flyway Strategy

Each service should configure Flyway with a unique history table and service-specific migration path:

```
spring.flyway.enabled=true
spring.flyway.table=<service>_flyway_history
spring.flyway.locations=classpath:db/migration/<service>
```

Example (user-service):

```
spring.flyway.enabled=true
spring.flyway.table=user_service_flyway_history
spring.flyway.locations=classpath:db/migration/user-service
```
