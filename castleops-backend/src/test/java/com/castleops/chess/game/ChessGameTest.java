
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

    @Test
    void shouldRejectIgnoringCheckAndAllowBlockingIt() {
        assertTrue(game.makeMove(createMove("f2", "f3")));
        assertTrue(game.makeMove(createMove("e7", "e5")));
        assertTrue(game.makeMove(createMove("a2", "a3")));
        assertTrue(game.makeMove(createMove("d8", "h4")));

        assertFalse(game.makeMove(createMove("a3", "a4")));
        assertEquals(PieceColor.WHITE, game.getCurrentTurn());
        assertNotNull(game.getPiece(Position.fromAlgebraic("a3")));
        assertNull(game.getPiece(Position.fromAlgebraic("a4")));
        assertTrue(game.makeMove(createMove("g2", "g3")));
    }

    @Test
    void shouldRejectKingMovingIntoAttackAndRestoreKingPosition() {
        assertTrue(game.makeMove(createMove("f2", "f3")));
        assertTrue(game.makeMove(createMove("e7", "e5")));
        assertTrue(game.makeMove(createMove("a2", "a3")));
        assertTrue(game.makeMove(createMove("d8", "h4")));

        assertFalse(game.makeMove(createMove("e1", "f2")));
        assertEquals(new Piece(PieceType.KING, PieceColor.WHITE),
                game.getPiece(Position.fromAlgebraic("e1")));
        assertNull(game.getPiece(Position.fromAlgebraic("f2")));
        // This block only works if the tracked king position was restored to e1.
        assertTrue(game.makeMove(createMove("g2", "g3")));
    }

    @Test
    void shouldRestoreCapturedPieceWhenCaptureDoesNotResolveCheck() {
        assertTrue(game.makeMove(createMove("f2", "f3")));
        assertTrue(game.makeMove(createMove("e7", "e5")));
        assertTrue(game.makeMove(createMove("a2", "a3")));
        assertTrue(game.makeMove(createMove("b8", "c6")));
        assertTrue(game.makeMove(createMove("b2", "b3")));
        assertTrue(game.makeMove(createMove("c6", "b4")));
        assertTrue(game.makeMove(createMove("h2", "h3")));
        assertTrue(game.makeMove(createMove("d8", "h4")));

        assertFalse(game.makeMove(createMove("a3", "b4")));
        assertEquals(new Piece(PieceType.PAWN, PieceColor.WHITE),
                game.getPiece(Position.fromAlgebraic("a3")));
        assertEquals(new Piece(PieceType.KNIGHT, PieceColor.BLACK),
                game.getPiece(Position.fromAlgebraic("b4")));
        assertEquals(PieceColor.WHITE, game.getCurrentTurn());
    }

    @Test
    void shouldDetectFoolsMate() {
        assertTrue(game.makeMove(createMove("f2", "f3")));
        assertTrue(game.makeMove(createMove("e7", "e5")));
        assertTrue(game.makeMove(createMove("g2", "g4")));
        assertTrue(game.makeMove(createMove("d8", "h4")));

        assertEquals(GameStatus.CHECKMATE, game.getStatus());
        assertEquals(PieceColor.WHITE, game.getCurrentTurn());
    }

    @Test
    void shouldRejectMovesAfterCheckmate() {
        assertTrue(game.makeMove(createMove("f2", "f3")));
        assertTrue(game.makeMove(createMove("e7", "e5")));
        assertTrue(game.makeMove(createMove("g2", "g4")));
        assertTrue(game.makeMove(createMove("d8", "h4")));

        Piece pawn = game.getPiece(Position.fromAlgebraic("a2"));

        assertFalse(game.makeMove(createMove("a2", "a3")));

        assertEquals(pawn, game.getPiece(Position.fromAlgebraic("a2")));
        assertNull(game.getPiece(Position.fromAlgebraic("a3")));
        assertEquals(PieceColor.WHITE, game.getCurrentTurn());
        assertEquals(GameStatus.CHECKMATE, game.getStatus());
    }

    @Test
    void shouldExecutePromotionAndReplacePawn() {
        assertTrue(game.makeMove(createMove("a2", "a4")));
        assertTrue(game.makeMove(createMove("h7", "h5")));

        assertTrue(game.makeMove(createMove("a4", "a5")));
        assertTrue(game.makeMove(createMove("h5", "h4")));

        assertTrue(game.makeMove(createMove("a5", "a6")));
        assertTrue(game.makeMove(createMove("h4", "h3")));

        assertTrue(game.makeMove(createMove("a6", "b7")));
        assertTrue(game.makeMove(createMove("h3", "g2")));

        // Последният ред изисква избор на фигура.
        assertFalse(game.makeMove(createMove("b7", "a8")));
        assertEquals(PieceColor.WHITE, game.getCurrentTurn());

        Move promotion = new Move(
                Position.fromAlgebraic("b7"),
                Position.fromAlgebraic("a8"),
                PieceType.KNIGHT
        );

        assertTrue(game.makeMove(promotion));

        assertNull(game.getPiece(Position.fromAlgebraic("b7")));
        assertEquals(
                new Piece(PieceType.KNIGHT, PieceColor.WHITE),
                game.getPiece(Position.fromAlgebraic("a8"))
        );
        assertEquals(PieceColor.BLACK, game.getCurrentTurn());
    }

    private Move createMove(String from, String to) {
        return new Move(
                Position.fromAlgebraic(from),
                Position.fromAlgebraic(to)
        );
    }
}
