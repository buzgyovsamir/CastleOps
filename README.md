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
not fully recognized, including on resignation. Service/API, WebSocket, authentication,
persistence and clocks are future work.

ChessGame is the application entry point. GameState and Board are mutable internals.
The package-private state constructor transfers ownership without copying and starts
new position history; it is not a complete game restore API. MoveExecutor requires
validated moves and does not manage turns, history or results. Legal-move queries
simulate on live state and restore it. Future integrations must serialize all access
to each game, including queries and snapshot creation.

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
