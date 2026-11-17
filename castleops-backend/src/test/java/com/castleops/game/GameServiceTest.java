package com.castleops.game;

import com.castleops.chess.model.Move;
import com.castleops.chess.model.Piece;
import com.castleops.chess.model.PieceColor;
import com.castleops.chess.model.PieceType;
import com.castleops.chess.model.Position;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GameServiceTest {

    private final GameService service = new GameService();

    @Test
    void shouldCreateAndRetrieveGame() {
        UUID creatorId = UUID.randomUUID();

        GameSnapshot created = service.createGame(creatorId);
        GameSnapshot retrieved = service.getGame(created.gameId());

        assertAll(
                () -> assertEquals(creatorId, created.whitePlayerId()),
                () -> assertEquals(
                        GameSessionStatus.WAITING_FOR_OPPONENT,
                        created.status()
                ),
                () -> assertEquals(created, retrieved)
        );
    }

    @Test
    void shouldJoinExistingGame() {
        GameSnapshot created = service.createGame(UUID.randomUUID());
        UUID opponentId = UUID.randomUUID();

        GameSnapshot joined = service.joinGame(
                created.gameId(), opponentId
        );

        assertAll(
                () -> assertEquals(created.gameId(), joined.gameId()),
                () -> assertEquals(
                        opponentId,
                        joined.blackPlayerId().orElseThrow()
                ),
                () -> assertEquals(
                        GameSessionStatus.IN_PROGRESS,
                        joined.status()
                ),
                () -> assertEquals(
                        joined,
                        service.getGame(created.gameId())
                )
        );
    }

    @Test
    void shouldReturnUpdatedSnapshotAfterMove() {
        UUID whitePlayerId = UUID.randomUUID();
        GameSnapshot created = service.createGame(whitePlayerId);

        service.joinGame(created.gameId(), UUID.randomUUID());

        GameSnapshot moved = service.makeMove(
                created.gameId(),
                whitePlayerId,
                move("e2", "e4")
        );

        assertAll(
                () -> assertEquals(
                        PieceColor.BLACK,
                        moved.currentTurn()
                ),
                () -> assertFalse(
                        moved.pieces().containsKey(position("e2"))
                ),
                () -> assertEquals(
                        new Piece(PieceType.PAWN, PieceColor.WHITE),
                        moved.pieces().get(position("e4"))
                ),
                () -> assertEquals(
                        moved,
                        service.getGame(created.gameId())
                )
        );
    }

    @Test
    void shouldKeepGamesIndependent() {
        UUID whitePlayerId = UUID.randomUUID();

        GameSnapshot first = service.createGame(whitePlayerId);
        GameSnapshot second = service.createGame(whitePlayerId);

        assertNotEquals(first.gameId(), second.gameId());

        service.joinGame(first.gameId(), UUID.randomUUID());
        service.makeMove(
                first.gameId(),
                whitePlayerId,
                move("e2", "e4")
        );

        assertEquals(second, service.getGame(second.gameId()));
    }

    @Test
    void shouldRejectOperationsOnUnknownGame() {
        UUID unknownGameId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();

        assertAll(
                () -> assertThrows(
                        GameNotFoundException.class,
                        () -> service.getGame(unknownGameId)
                ),
                () -> assertThrows(
                        GameNotFoundException.class,
                        () -> service.joinGame(unknownGameId, playerId)
                ),
                () -> assertThrows(
                        GameNotFoundException.class,
                        () -> service.makeMove(
                                unknownGameId,
                                playerId,
                                move("e2", "e4")
                        )
                )
        );
    }

    @Test
    void shouldPreserveGameAfterUnauthorizedMove() {
        GameSnapshot created = service.createGame(UUID.randomUUID());

        GameSnapshot before = service.joinGame(
                created.gameId(), UUID.randomUUID()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.makeMove(
                        created.gameId(),
                        UUID.randomUUID(),
                        move("e2", "e4")
                )
        );

        assertEquals(before, service.getGame(created.gameId()));
    }

    @Test
    void shouldPreserveGameAfterIllegalMove() {
        UUID whitePlayerId = UUID.randomUUID();
        GameSnapshot created = service.createGame(whitePlayerId);

        GameSnapshot before = service.joinGame(
                created.gameId(), UUID.randomUUID()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.makeMove(
                        created.gameId(),
                        whitePlayerId,
                        move("e2", "e5")
                )
        );

        assertEquals(before, service.getGame(created.gameId()));
    }

    private Position position(String square) {
        return Position.fromAlgebraic(square);
    }

    private Move move(String from, String to) {
        return new Move(position(from), position(to));
    }
}