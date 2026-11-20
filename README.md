# CastleOps

Portfolio project for Junior Developer / DevOps roles: online chess for two players.
Java 21, Spring Boot and Maven. The chess core has no Spring or transport dependencies.

## Current capabilities

All piece movements and captures; king safety, checkmate and stalemate; castling
and rights; en passant; four promotions; temporary move application/undo; immutable
public legal-move lists; fifty-move claims and automatic seventy-five-move draws;
threefold claims and automatic fivefold repetition; basic insufficient material;
draw offers, resignation with material exceptions, winner and end reason.

## Product decisions

Draw offers are made on the offering player's turn. The opponent can respond
immediately, even before the first move. An offer survives the offering player's
move and expires on the opponent's legal move or game completion. Illegal moves
and rejected claims preserve pending offers.

Claims require the claimant's turn. Accepted intended-move claims end the game
without playing that move. Rejected claims leave the position unchanged. This is
an online product model, not the full tournament procedure involving arbiters,
penalties and binding intended moves. Checkmate takes precedence over the
seventy-five-move threshold. Inability to force mate alone is not insufficient material.

## Boundaries

Complex dead positions depending on arrangement and blocked pawns are deliberately
not fully recognized, including on resignation. Authentication,
persistence and clocks are future work.

ChessGame is the application entry point. GameState and Board are mutable internals.
The package-private state constructor transfers ownership without copying and starts
new position history; it is not a complete game restore API. MoveExecutor requires
validated moves and does not manage turns, history or results. Legal-move queries
simulate on live state and restore it. Future integrations must serialize all access
to each game, including queries and snapshot creation.

## REST API and real-time updates

Start the backend from `castleops-backend` with `.\mvnw.cmd spring-boot:run`
on Windows or `./mvnw spring-boot:run` on Linux/macOS.
The local API is available at `http://localhost:8080/api/games`.

| Method | Path | Action |
|---|---|---|
| `POST` | `/api/games` | Create a game as White |
| `POST` | `/api/games/{gameId}/join` | Join as Black |
| `GET` | `/api/games/{gameId}` | Read the current snapshot |
| `POST` | `/api/games/{gameId}/moves` | Play a move |

Send POST requests as `application/json`. Create and join requests take a
`playerId` (UUID). Use a different player ID for each side. A move request
looks like this:

```json
{
  "playerId": "febff8a3-dc52-4f8b-a0c8-287ee716ea87",
  "from": "e2",
  "to": "e4"
}
```

Use the ID of the player making the move. For promotion, also supply
`promotionType`, for example `"QUEEN"`.

### Try a game with two subscribers

1. Create a game through REST and copy the `gameId` from the response.
   The initial snapshot has `version: 0`.
2. Connect two STOMP clients to `ws://localhost:8080/ws`. Subscribe both to
   `/topic/games/{gameId}`, replacing `{gameId}` with the ID from step 1.
3. Once both subscriptions are active, run the following requests through REST.

| Action | Expected HTTP result | Expected update on both clients |
|---|---|---|
| Black joins | `200 OK` | Version 1, `IN_PROGRESS`, White to move |
| White plays `e2` to `e4` | `200 OK` | Version 2, white pawn on `e4`, Black to move |
| White attempts `d2` to `d4` immediately afterward | `409 Conflict` | No update; the game stays at version 2 |
| Black plays `e7` to `e5` | `200 OK` | Version 3, black pawn on `e5`, White to move |

Each successful join or move publishes the same `GameResponse` returned by
REST. After reconnecting, subscribe and fetch the current snapshot with `GET`.
Keep the highest version received for that game so a delayed response cannot
roll the board back.

Games are stored in memory and are lost on restart. The current API accepts
player IDs supplied by the client. Authentication is planned.

## Tests

Select JDK 21 through JAVA_HOME, then run from castleops-backend:

```powershell
.\mvnw.cmd test
```

Linux/macOS:

```sh
./mvnw test
```

The wrapper downloads Maven and dependencies when needed. Unit and integration
tests cover the core. Initial-position perft through depth four verifies move
counts and restoration, but does not exercise played castling, en passant captures
or promotions. Dedicated tests cover these moves. The suite does not establish
complete rule coverage or concurrent-use safety.

## Test the api locally with Postman


| Действие | Заявка | Очакван резултат |
|---|---|---|
| Създай партия като белите | `POST /api/games` | `201`, `gameId`, `WAITING_FOR_OPPONENT` |
| Присъедини черните | `POST /api/games/{gameId}/join` | `200`, `IN_PROGRESS`, белите са на ход |
| Изиграй `e2 → e4` | `POST /api/games/{gameId}/moves` | `200`, черните са на ход |
| Опитай `d2 → d4` пак като белите | `POST /api/games/{gameId}/moves` | `409` |
| Провери дъската след отказа | `GET /api/games/{gameId}` | Пешката остава на `d2`, черните още са на ход |
| Изиграй `e7 → e5` като черните | `POST /api/games/{gameId}/moves` | `200`, белите са на ход |
| Подай `e9` като начално поле | `POST /api/games/{gameId}/moves` | `400` |
