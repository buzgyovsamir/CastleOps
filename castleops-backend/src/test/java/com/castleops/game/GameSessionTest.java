package com.castleops.game;

import com.castleops.chess.model.Move;
import com.castleops.chess.model.Position;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GameSessionTest {

    @Test
    void shouldCreateGameWaitingForOpponent() {
        UUID gameId = UUID.randomUUID();
        UUID creatorId = UUID.randomUUID();

        GameSession session = new GameSession(gameId, creatorId);

        assertAll(
                () -> assertEquals(gameId, session.getId()),
                () -> assertEquals(creatorId, session.getWhitePlayerId()),
                () -> assertTrue(session.getBlackPlayerId().isEmpty()),
                () -> assertEquals(
                        GameSessionStatus.WAITING_FOR_OPPONENT,
                        session.getStatus()
                )
        );
    }

    @Test
    void shouldAssignJoiningPlayerToBlackAndStartGame() {
        UUID creatorId = UUID.randomUUID();
        UUID opponentId = UUID.randomUUID();
        GameSession session = new GameSession(
                UUID.randomUUID(), creatorId
        );

        session.join(opponentId);

        assertAll(
                () -> assertEquals(
                        opponentId,
                        session.getBlackPlayerId().orElseThrow()
                ),
                () -> assertEquals(
                        creatorId,
                        session.getWhitePlayerId()
                ),
                () -> assertEquals(
                        GameSessionStatus.IN_PROGRESS,
                        session.getStatus()
                )
        );
    }

    @Test
    void shouldRejectCreatorJoiningAsOpponent() {
        UUID creatorId = UUID.randomUUID();
        GameSession session = new GameSession(
                UUID.randomUUID(), creatorId
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> session.join(creatorId)
        );

        assertTrue(session.getBlackPlayerId().isEmpty());
        assertEquals(
                GameSessionStatus.WAITING_FOR_OPPONENT,
                session.getStatus()
        );
    }

    @Test
    void shouldRejectThirdPlayerWithoutReplacingOpponent() {
        GameSession session = new GameSession(
                UUID.randomUUID(), UUID.randomUUID()
        );
        UUID opponentId = UUID.randomUUID();
        session.join(opponentId);

        assertThrows(
                IllegalStateException.class,
                () -> session.join(UUID.randomUUID())
        );

        assertEquals(
                opponentId,
                session.getBlackPlayerId().orElseThrow()
        );
        assertEquals(
                GameSessionStatus.IN_PROGRESS,
                session.getStatus()
        );
    }

    @Test
    void shouldRejectNullPlayerWithoutChangingSession() {
        GameSession session = new GameSession(
                UUID.randomUUID(), UUID.randomUUID()
        );

        assertThrows(
                NullPointerException.class,
                () -> session.join(null)
        );

        assertTrue(session.getBlackPlayerId().isEmpty());
        assertEquals(
                GameSessionStatus.WAITING_FOR_OPPONENT,
                session.getStatus()
        );
    }

    @Test
    void shouldRejectMissingConstructorArguments() {
        assertAll(
                () -> assertThrows(
                        NullPointerException.class,
                        () -> new GameSession(null, UUID.randomUUID())
                ),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> new GameSession(UUID.randomUUID(), null)
                )
        );
    }

    @Test
    void shouldAllowPlayersToAlternateLegalMoves() {
        UUID whitePlayerId = UUID.randomUUID();
        UUID blackPlayerId = UUID.randomUUID();

        GameSession session = new GameSession(
                UUID.randomUUID(), whitePlayerId
        );
        session.join(blackPlayerId);

        assertTrue(session.makeMove(
                whitePlayerId, move("e2", "e4")
        ));
        assertTrue(session.makeMove(
                blackPlayerId, move("e7", "e5")
        ));
        assertTrue(session.makeMove(
                whitePlayerId, move("g1", "f3")
        ));
    }

    @Test
    void shouldRejectMoveBeforeOpponentJoins() {
        UUID whitePlayerId = UUID.randomUUID();

        GameSession session = new GameSession(
                UUID.randomUUID(), whitePlayerId
        );

        assertThrows(
                IllegalStateException.class,
                () -> session.makeMove(
                        whitePlayerId, move("e2", "e4")
                )
        );

        session.join(UUID.randomUUID());

        // The rejected command must not have moved the pawn.
        assertTrue(session.makeMove(
                whitePlayerId, move("e2", "e4")
        ));
    }

    @Test
    void shouldRejectMoveFromNonParticipant() {
        UUID whitePlayerId = UUID.randomUUID();

        GameSession session = new GameSession(
                UUID.randomUUID(), whitePlayerId
        );
        session.join(UUID.randomUUID());

        assertThrows(
                IllegalArgumentException.class,
                () -> session.makeMove(
                        UUID.randomUUID(), move("e2", "e4")
                )
        );

        assertTrue(session.makeMove(
                whitePlayerId, move("e2", "e4")
        ));
    }

    @Test
    void shouldRejectPlayerMovingOutOfTurn() {
        UUID whitePlayerId = UUID.randomUUID();
        UUID blackPlayerId = UUID.randomUUID();

        GameSession session = new GameSession(
                UUID.randomUUID(), whitePlayerId
        );
        session.join(blackPlayerId);

        assertThrows(
                IllegalStateException.class,
                () -> session.makeMove(
                        blackPlayerId, move("e7", "e5")
                )
        );

        assertTrue(session.makeMove(
                whitePlayerId, move("e2", "e4")
        ));

        assertThrows(
                IllegalStateException.class,
                () -> session.makeMove(
                        whitePlayerId, move("d2", "d4")
                )
        );

        assertTrue(session.makeMove(
                blackPlayerId, move("e7", "e5")
        ));
    }

    @Test
    void shouldRejectMovingOpponentsPieceWithoutConsumingTurn() {
        UUID whitePlayerId = UUID.randomUUID();

        GameSession session = new GameSession(
                UUID.randomUUID(), whitePlayerId
        );
        session.join(UUID.randomUUID());

        assertFalse(session.makeMove(
                whitePlayerId, move("e7", "e5")
        ));

        assertTrue(session.makeMove(
                whitePlayerId, move("e2", "e4")
        ));
    }

    @Test
    void shouldRejectIllegalMoveWithoutConsumingTurn() {
        UUID whitePlayerId = UUID.randomUUID();

        GameSession session = new GameSession(
                UUID.randomUUID(), whitePlayerId
        );
        session.join(UUID.randomUUID());

        assertFalse(session.makeMove(
                whitePlayerId, move("e2", "e5")
        ));

        assertTrue(session.makeMove(
                whitePlayerId, move("e2", "e4")
        ));
    }

    @Test
    void shouldFinishSessionAfterCheckmateAndRejectFurtherMoves() {
        UUID whitePlayerId = UUID.randomUUID();
        UUID blackPlayerId = UUID.randomUUID();

        GameSession session = new GameSession(
                UUID.randomUUID(), whitePlayerId
        );
        session.join(blackPlayerId);

        // Fool's mate.
        assertTrue(session.makeMove(
                whitePlayerId, move("f2", "f3")
        ));
        assertTrue(session.makeMove(
                blackPlayerId, move("e7", "e5")
        ));
        assertTrue(session.makeMove(
                whitePlayerId, move("g2", "g4")
        ));
        assertTrue(session.makeMove(
                blackPlayerId, move("d8", "h4")
        ));

        assertEquals(
                GameSessionStatus.FINISHED,
                session.getStatus()
        );

        assertThrows(
                IllegalStateException.class,
                () -> session.makeMove(
                        whitePlayerId, move("a2", "a3")
                )
        );
    }

    private Move move(String from, String to) {
        return new Move(
                Position.fromAlgebraic(from),
                Position.fromAlgebraic(to)
        );
    }
}