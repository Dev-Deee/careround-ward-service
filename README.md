# careround-ward-service

Wards, shifts, attendance, charge nurses and handovers. HTTP 8083, gRPC 9093, DB `careround_ward_db`.

- **REST:** `/api/v1/wards`, `/api/v1/shift-definitions`, `/api/v1/staffing-requirements`, `/api/v1/attendance`,
  `/api/v1/charge-assignments`, `/api/v1/charge-succession`, `/api/v1/handovers`.
- **gRPC server (9093):** `WardVerificationService.WardExists` (by ward `publicId`; used by auth, patient and round to
  verify ward ids), `ChargeVerificationService.IsActiveChargeNurse`.
- **gRPC clients:** patient-service (`GetAdmittedCount`, ward census), user-service (`GetMostSeniorNurse`).
- **Scheduled:** `ChargeSuccessionScheduler` (fills a vacant charge role when its window closes).
- The ward id used across services is the ward's **publicId**.

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
