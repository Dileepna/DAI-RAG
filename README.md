# Wind Farm Measurement RAG

A single Spring Boot 3 / Java 21 service following the ingestion service's hexagonal architecture. The same application exposes upload and query endpoints, stores embeddings in Qdrant, stores ingestion metadata in PostgreSQL, and uses Ollama for chat and embeddings.

## Run locally

From the project root run `mvn spring-boot:run`, or start dependencies and the application with `docker compose up --build`. Ollama uses `llama3.2` for chat and `nomic-embed-text` for embeddings. Both are pulled automatically when missing; manual commands are `docker compose exec ollama ollama pull llama3.2` and `docker compose exec ollama ollama pull nomic-embed-text`.

## API

- `POST /llm/documents/upload`: multipart field `file`, a ZIP containing `.xlsx` or `.xlsm` workbooks.
- `POST /llm/query`: JSON such as `{"prompt":"What was the peak wind speed for T-01?","topK":5,"deviceUid":"T-01"}`.

OpenAPI UI: `/swagger-ui/index.html`. Health: `/actuator/health/readiness` and `/actuator/health/liveness`.

## Architecture

`application/port/in` contains use cases, `application/port/out` contains infrastructure interfaces, `application/service` contains orchestration, `domain` contains measurement and chunking rules, and `infrastructure/adapter` contains Excel, Qdrant, PostgreSQL, Ollama and REST adapters. The domain/application layers do not depend on Spring or vendor APIs.

## Docker and Kubernetes

Build the single image with `docker build -t windfarm-rag:1.0.0 .`. Kubernetes uses `k8s/dai-rag.yaml`; it expects externally managed PostgreSQL, Qdrant and Ollama, plus a `windfarm-secrets` Secret containing `database-user` and `database-password`. Replace the example hostnames before deploying.
