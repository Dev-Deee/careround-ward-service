# careround-ward-service

Wards, shifts, attendance, charge nurses and handovers. HTTP 8083, gRPC 9093, DB `careround_ward_db`.

- **REST:** `/api/v1/wards`, `/api/v1/shift-definitions`, `/api/v1/staffing-requirements`, `/api/v1/attendance`,
  `/api/v1/charge-assignments`, `/api/v1/charge-succession`, `/api/v1/handovers`.
- **gRPC server (9093):** `WardVerificationService.WardExists` (by ward `publicId`; used by auth, patient and round to
  verify ward ids), `ChargeVerificationService.IsActiveChargeNurse`.
- **gRPC clients:** patient-service (`GetAdmittedCount`, ward census), user-service (`GetMostSeniorNurse`).
- **Scheduled:** `ChargeSuccessionScheduler` (fills a vacant charge role when its window closes).
- The ward id used across services is the ward's **publicId**.

## Read side

Paged lists use `page` (0-based) and `size` (default 20, max 100; larger is clamped) and return
`{items, page, size, total}`.

| Endpoint | Who | Notes |
|---|---|---|
| `GET /api/v1/attendance?wardId&status&role&page&size` | ADMIN, or the ward's active charge nurse | `status` `CLOCKED_IN`/`CLOCKED_OUT`, `role` `NURSE`/`DOCTOR`, both optional. Newest `clockInAt` first. Items add `staffRole`. |
| `GET /api/v1/handovers?wardId&status&page&size` | ADMIN, or the ward's active charge nurse | `status` `PENDING`/`HANDED_OVER`. Newest `initiatedAt` first. |
| `GET /api/v1/handovers/{publicId}` | the outgoing/incoming nurse named on it, the ward's active charge nurse, or ADMIN | Handover plus shift names and `notes [{id, patientId, note, urgency}]`, URGENT → WATCH → ROUTINE. |
| `GET /api/v1/wards?page&size` | any authenticated | With `page` or `size`: the envelope, sorted by name (the database's `en_US` collation, which ignores spaces at the first level). Without either: the old bare array (**deprecated**, kept for existing callers). |

Unknown ward → 404; a non-supervisor → 403; missing `wardId` or a bad enum → 400.

**PRD path → built path.**

| PRD | Built |
|---|---|
| `GET /wards/{wardId}/handover` (shift handover view) | `GET /api/v1/handovers?wardId=` + `GET /api/v1/handovers/{publicId}` |
| `GET /admin/wards`, `GET /admin/wards/{id}` | `GET /api/v1/wards?page=&size=`, `GET /api/v1/wards/{publicId}` |
| — (who is on duty) | `GET /api/v1/attendance?wardId=&status=CLOCKED_IN` |

## Errors

Every error uses the body `{"status", "error", "message", "timestamp"}`.

- A path no endpoint maps (e.g. a collection root with a trailing slash, or a wrong sub-path) → **404**
  `No endpoint <METHOD> <path>`. A wrong HTTP method on a mapped path → 405, unsupported content type → 415,
  malformed JSON → 400.
- Anything unexpected → 500 with a generic message; the exception is logged at ERROR with its stack trace.

## Environment variables

Copy `.env.example` to `.env` (gitignored) and fill in real values. `main()` loads `.env` into system properties;
plain environment variables work too.

| Variable | Purpose |
|---|---|
| `DB_USERNAME` | Postgres user (root docker-compose: careround-postgres) |
| `DB_PASSWORD` | Postgres password |
| `JWT_PUBLIC_KEY` | RSA public key used to verify access tokens; same value in every service. PEM on one line, `|` for newlines |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka (root docker-compose: careround-kafka) |

## Run

Needs the root `docker-compose.yml` stack (Postgres, Kafka, Redis) and a `.env`.

```bash
./mvnw clean package -DskipTests && java -jar target/careround-ward-service-0.0.1-SNAPSHOT.jar
```

Clients reach the REST API through the gateway on `http://localhost:8080`; the service port also works directly.
