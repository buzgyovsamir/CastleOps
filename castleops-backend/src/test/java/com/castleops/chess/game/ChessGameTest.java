
package com.castleops.chess.game;

import com.castleops.chess.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChessGameTest {

    private ChessGame game;

    @BeforeEach
    void setUp() {
        game = new ChessGame();
    }

    @Test
    void shouldExecuteValidPawnMove() {
        Move move = createMove("e2", "e4");

        boolean result = game.makeMove(move);

        assertTrue(result);
        assertNull(game.getPiece(
                Position.fromAlgebraic("e2")
        ));

        assertEquals(
                new Piece(PieceType.PAWN, PieceColor.WHITE),
                game.getPiece(Position.fromAlgebraic("e4"))
        );
    }

    @Test
    void shouldSwitchTurnAfterSuccessfulMove() {
        game.makeMove(createMove("e2", "e4"));

        assertEquals(
                PieceColor.BLACK,
                game.getCurrentTurn()
        );
    }

    @Test
    void shouldRejectMoveFromWrongPlayer() {
        Move move = createMove("e7", "e5");

        boolean result = game.makeMove(move);

        assertFalse(result);
        assertEquals(
                PieceColor.WHITE,
                game.getCurrentTurn()
        );
    }

    @Test
    void shouldNotChangeBoardAfterInvalidMove() {
        Move move = createMove("e2", "e5");

        boolean result = game.makeMove(move);

        assertFalse(result);

        assertEquals(
                new Piece(PieceType.PAWN, PieceColor.WHITE),
                game.getPiece(Position.fromAlgebraic("e2"))
        );

        assertNull(game.getPiece(
                Position.fromAlgebraic("e5")
        ));

        assertEquals(
                PieceColor.WHITE,
                game.getCurrentTurn()
        );
    }

    @Test
    void shouldAlternateTurnsBetweenPlayers() {
        assertTrue(game.makeMove(createMove("e2", "e4")));
        assertTrue(game.makeMove(createMove("e7", "e5")));

        assertEquals(
                PieceColor.WHITE,
                game.getCurrentTurn()
        );
    }

    private Move createMove(String from, String to) {
        return new Move(
                Position.fromAlgebraic(from),
                Position.fromAlgebraic(to)
        );
    }
}
