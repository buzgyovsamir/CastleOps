package com.castleops.game;

import com.castleops.chess.game.GameEndReason;
import com.castleops.chess.model.Move;
import com.castleops.chess.model.Piece;
import com.castleops.chess.model.PieceColor;
import com.castleops.chess.model.PieceType;
import com.castleops.chess.model.Position;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GameSessionSnapshotTest {

    @Test
    void shouldDescribeNewSession() {
        UUID gameId = UUID.randomUUID();
        UUID whitePlayerId = UUID.randomUUID();

        GameSession session = new GameSession(gameId, whitePlayerId);

        GameSnapshot snapshot = session.getSnapshot();

        assertAll(
                () -> assertEquals(gameId, snapshot.gameId()),
                () -> assertEquals(whitePlayerId, snapshot.whitePlayerId()),
                () -> assertTrue(snapshot.blackPlayerId().isEmpty()),
                () -> assertEquals(
                        GameSessionStatus.WAITING_FOR_OPPONENT,
                        snapshot.status()
                ),
                () -> assertEquals(
                        PieceColor.WHITE,
                        snapshot.currentTurn()
                ),
                () -> assertEquals(32, snapshot.pieces().size()),
                () -> assertEquals(
                        new Piece(PieceType.KING, PieceColor.WHITE),
                        snapshot.pieces().get(position("e1"))
                ),
                () -> assertTrue(snapshot.result().isEmpty()),
                () -> assertTrue(snapshot.drawOfferBy().isEmpty())
        );
    }

    @Test
    void shouldKeepPreviousSnapshotUnchangedAfterJoinAndMove() {
        UUID whitePlayerId = UUID.randomUUID();
        UUID blackPlayerId = UUID.randomUUID();

        GameSession session = new GameSession(
                UUID.randomUUID(), whitePlayerId
        );
        GameSnapshot before = session.getSnapshot();

        session.join(blackPlayerId);

        GameSnapshot after = session.makeMoveAndGetSnapshot(
                whitePlayerId, move("e2", "e4")
        );

        Piece whitePawn = new Piece(PieceType.PAWN, PieceColor.WHITE);

        assertAll(
                () -> assertEquals(
                        GameSessionStatus.WAITING_FOR_OPPONENT,
                        before.status()
                ),
                () -> assertTrue(before.blackPlayerId().isEmpty()),
                () -> assertEquals(PieceColor.WHITE, before.currentTurn()),
                () -> assertEquals(
                        whitePawn,
                        before.pieces().get(position("e2"))
                ),
                () -> assertFalse(
                        before.pieces().containsKey(position("e4"))
                ),
                () -> assertEquals(
                        GameSessionStatus.IN_PROGRESS,
                        after.status()
                ),
                () -> assertEquals(
                        blackPlayerId,
                        after.blackPlayerId().orElseThrow()
                ),
                () -> assertEquals(PieceColor.BLACK, after.currentTurn()),
                () -> assertFalse(
                        after.pieces().containsKey(position("e2"))
                ),
                () -> assertEquals(
                        whitePawn,
                        after.pieces().get(position("e4"))
                )
        );
    }

    @Test
    void shouldPreventSnapshotBoardMutation() {
        GameSession session = new GameSession(
                UUID.randomUUID(), UUID.randomUUID()
        );

        GameSnapshot snapshot = session.getSnapshot();

        assertThrows(
                UnsupportedOperationException.class,
                () -> snapshot.pieces().remove(position("e1"))
        );

        assertEquals(32, session.getSnapshot().pieces().size());
    }

    @Test
    void shouldPreserveSnapshotAfterRejectedMove() {
        UUID whitePlayerId = UUID.randomUUID();

        GameSession session = new GameSession(
                UUID.randomUUID(), whitePlayerId
        );
        session.join(UUID.randomUUID());

        GameSnapshot before = session.getSnapshot();

        assertThrows(
                IllegalArgumentException.class,
                () -> session.makeMoveAndGetSnapshot(
                        whitePlayerId, move("e2", "e5")
                )
        );

        assertEquals(before, session.getSnapshot());
    }

    @Test
    void shouldIncludeWinnerAndReasonAfterCheckmate() {
        UUID whitePlayerId = UUID.randomUUID();
        UUID blackPlayerId = UUID.randomUUID();

        GameSession session = new GameSession(
                UUID.randomUUID(), whitePlayerId
        );
        session.join(blackPlayerId);

        session.makeMoveAndGetSnapshot(
                whitePlayerId, move("f2", "f3")
        );
        session.makeMoveAndGetSnapshot(
                blackPlayerId, move("e7", "e5")
        );
        session.makeMoveAndGetSnapshot(
                whitePlayerId, move("g2", "g4")
        );

        GameSnapshot snapshot = session.makeMoveAndGetSnapshot(
                blackPlayerId, move("d8", "h4")
        );

        assertEquals(GameSessionStatus.FINISHED, snapshot.status());

        var result = snapshot.result().orElseThrow();

        assertEquals(GameEndReason.CHECKMATE, result.reason());
        assertEquals(PieceColor.BLACK, result.winner());
    }

    private Position position(String square) {
        return Position.fromAlgebraic(square);
    }

    private Move move(String from, String to) {
        return new Move(position(from), position(to));
    }
}