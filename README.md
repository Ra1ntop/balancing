# balancing

Matchmaking playground. Two Gradle modules:

| Module         | What it is                                                    |
|----------------|---------------------------------------------------------------|
| `balance-core` | Plain Java module, home for the balancing logic               |
| `balance-api`  | Spring Boot 4 REST API storing players and their statistics   |

## Requirements

- JDK 21 (the build pins the toolchain, so any newer JDK works for running Gradle)
- Docker — for the local database and for the integration tests

## Running

```bash
docker compose up -d          # Postgres on localhost:5432
./gradlew :balance-api:bootRun
```

`spring-boot-docker-compose` is on the development classpath, so `bootRun` starts
`compose.yaml` on its own — the explicit `docker compose up` is only needed if you want the
database without the application. Flyway applies the migrations at startup; Hibernate is set
to `validate` and never touches the schema.

## Tests

```bash
./gradlew test
```

Unit tests cover the statistics arithmetic. The API tests run against a real Postgres started
by Testcontainers, migrations included, so they need a working Docker daemon.

## Data model

`player` holds the profile, `player_statistics` holds the counters and shares the player's
primary key — strictly 1:1, and deleting a player deletes the statistics with it.

Only raw counters are stored: `wins`, `losses`, `draws`, `total_damage`, `current_win_streak`
and `current_loss_streak`. Matches played, win rate and average damage are derived on read, so
stored data can never disagree with what the API reports.

## API

| Method   | Path                          | Purpose                                      |
|----------|-------------------------------|----------------------------------------------|
| `POST`   | `/api/players`                | Create a player with zeroed statistics       |
| `GET`    | `/api/players`                | List players, paged                          |
| `GET`    | `/api/players/{id}`           | One player with statistics                   |
| `PUT`    | `/api/players/{id}`           | Rename a player                              |
| `DELETE` | `/api/players/{id}`           | Delete a player and the statistics           |
| `GET`    | `/api/players/{id}/statistics`| Statistics only                              |
| `POST`   | `/api/players/{id}/matches`   | Report a finished match                      |

Statistics change through reported matches, never by writing the numbers directly:

```bash
curl -X POST localhost:8080/api/players \
  -H 'Content-Type: application/json' \
  -d '{"nickname": "shroud", "displayName": "Michael"}'

curl -X POST localhost:8080/api/players/<id>/matches \
  -H 'Content-Type: application/json' \
  -d '{"result": "WIN", "damage": 1200}'
```

`result` is `WIN`, `LOSS` or `DRAW`. A win extends the win streak and clears the loss streak, a
loss does the opposite, and a draw ends both. The statistics row is locked for the duration of
the update, so matches reported concurrently for one player cannot overwrite each other.

Errors come back as RFC 9457 problem details: `404` for an unknown player, `409` for a taken
nickname, `400` with a per-field `errors` object for invalid input.
