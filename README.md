# CastleOps

CastleOps is a two-player online chess application built with React, TypeScript, Java 21 and Spring Boot. Two players sign in, share a game ID and play from separate browsers. The backend validates each move and sends the updated position to both boards over WebSocket.

## Run locally

Install Docker with Docker Compose, then clone the repository:

```sh
git clone https://github.com/buzgyovsamir/CastleOps.git
cd CastleOps
```

Create a `.env` file next to `compose.yaml`. Replace the placeholder with a local database password. If you already have a CastleOps PostgreSQL volume, use its existing password.

```dotenv
CASTLEOPS_DB_PASSWORD=replace_with_your_local_password
```

Keep `.env` out of Git. Choose one of the following ways to start the application.

### Build from source

```sh
docker compose up -d --build
docker compose ps
```

### Run the published images

The `compose.ghcr.yaml` override selects the frontend and backend images from GitHub Container Registry (GHCR). Both image references use the same commit SHA tag.

```sh
docker compose -f compose.yaml -f compose.ghcr.yaml pull backend frontend
docker compose -f compose.yaml -f compose.ghcr.yaml up -d --no-build
docker compose -f compose.yaml -f compose.ghcr.yaml ps
```

Open **http://localhost:8081** to play. Docker Compose binds all three services to localhost:

| Service | Host port | Purpose |
| --- | --- | --- |
| Frontend | `8081` | Browser entry point served by NGINX |
| Backend | `8080` | Direct access to the Spring Boot API |
| PostgreSQL | `5433` | Local database connections |

## Play a game

1. Open CastleOps in a regular browser window. Register or sign in, create a game as White and copy the game ID.
2. Open a private window or a separate browser profile. Sign in with a second account and join the game as Black.
3. As White, select the pawn on `e2`. The board marks its legal destinations. Move it to `e4` and watch the position update in Black's window.
4. As Black, reply with `e7` to `e5`. White should see the reply without refreshing the page.

Each player sees the board from their own side. The interface shows whose turn it is, highlights a king in check and displays the result when the game ends.

## Architecture

| Component | Responsibility |
| --- | --- |
| React, TypeScript and Vite | Account screens, lobby, interactive board and versioned game updates |
| NGINX | Serves the frontend and proxies API requests and WebSocket connections to Spring |
| Spring Boot and Spring Security | Session authentication, player authorization, game commands and STOMP updates |
| Java chess engine | Move generation, king safety, position history and game results. The engine is independent of Spring and the transport layer |
| PostgreSQL 17 and Flyway | Persistent account data and database schema migrations |

Active games live in the backend process. Restarting the backend clears those games. Accounts survive container recreation through the PostgreSQL volume.

## API and real-time updates

The server identifies the player from the authenticated session. When using an API client such as Postman, retain the session cookie. Before sending a POST request, call `GET /api/auth/csrf` and use the returned header name and token. Fetch a fresh token after signing in.

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/api/auth/csrf` | Obtain the CSRF header name and token |
| `POST` | `/api/auth/register` | Register an account |
| `POST` | `/api/auth/login` | Sign in with a form-encoded username and password |
| `GET` | `/api/auth/me` | Read the signed-in account. Guests receive `401 Unauthorized` |
| `POST` | `/api/auth/logout` | End the session |
| `POST` | `/api/games` | Create a game as White |
| `POST` | `/api/games/{gameId}/join` | Join an open game as Black |
| `GET` | `/api/games/{gameId}` | Read the current game snapshot |
| `POST` | `/api/games/{gameId}/moves` | Submit a move for the signed-in player |

For example, White can open with `e2` to `e4` by sending this JSON body to `POST /api/games/{gameId}/moves`:

```json
{
  "from": "e2",
  "to": "e4"
}
```

For promotion, include `promotionType` with one of `QUEEN`, `ROOK`, `BISHOP` or `KNIGHT`.

Clients connect to `/ws` using STOMP and subscribe to `/topic/games/{gameId}`. An accepted join or move returns a snapshot over HTTP and publishes it to subscribers. After reconnecting, the frontend subscribes again and fetches the latest snapshot. It compares version numbers so a delayed response cannot roll the board back to an earlier position.

## Chess rules

The engine supports castling, en passant, all four promotions, checkmate and stalemate. Draw rules include threefold-repetition and fifty-move claims, automatic fivefold-repetition and seventy-five-move draws, and basic insufficient-material detection. Checkmate takes precedence at the seventy-five-move threshold.

A player can offer a draw on their turn. The offer survives that player's move and expires when the opponent plays a legal move or the game ends. An accepted draw claim based on an intended move ends the game before that move is played.

Resignation takes the opponent's mating material into account. Complex dead positions involving blocked pawns remain a known limitation.

## Tests and CI

GitHub Actions runs the Maven verification suite against PostgreSQL and builds the frontend with Node.js 24. Tests cover the chess engine, API and session behavior, concurrent game access and WebSocket delivery. The engine tests check move counts from the initial position through depth four. Dedicated cases exercise castling, en passant and promotion.

### Backend verification

Use JDK 21 and a PostgreSQL test database. Set `CASTLEOPS_DB_URL`, `CASTLEOPS_DB_USERNAME` and `CASTLEOPS_DB_PASSWORD` in your shell for that database. From `castleops-backend`, run the Maven wrapper for your platform.

Windows PowerShell:

```powershell
.\mvnw.cmd verify
```

Linux or macOS:

```sh
./mvnw verify
```

### Frontend build

From `castleops-frontend`, run:

```sh
npm ci
npm run build
```

### Container publishing

After successful checks on a push to `main`, GitHub Actions publishes both application images to GHCR:

- `ghcr.io/buzgyovsamir/castleops-backend`
- `ghcr.io/buzgyovsamir/castleops-frontend`

Each image receives a `sha-<full-commit-sha>` tag. To run a newer published version locally, select a tag present on both packages, update both image references in `compose.ghcr.yaml` and repeat the published-image commands above.