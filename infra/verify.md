# Verify Infra

## Postgres (psql)
`docker compose --env-file .env -f docker-compose.yml exec -T postgres psql -U postgres -d swasthyasetu -c "SELECT 1;"`

## Redis (ping)
`docker compose --env-file .env -f docker-compose.yml exec -T redis redis-cli ping`

## Kafka (topics list)
`docker compose --env-file .env -f docker-compose.yml exec -T kafka kafka-topics --bootstrap-server localhost:9092 --list`

## MinIO Console (browser)
`Start-Process http://localhost:9001`
