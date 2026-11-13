
package com.castleops.chess.engine.validator.pieces;

import com.castleops.chess.game.GameState;
import com.castleops.chess.model.*;
import org.junit.jupiter.api.Test;

import static com.castleops.chess.support.GameStateFixtures.emptyGameState;
import static org.junit.jupiter.api.Assertions.*;

class KnightMoveValidatorTest {

    private final KnightMoveValidator validator = new KnightMoveValidator();

    @Test
    void shouldAllowValidLShapedMove() {
        GameState gameState = emptyGameState();
        Board board = gameState.getBoard();

        board.setPiece(
                Position.fromAlgebraic("e4"),
                new Piece(PieceType.KNIGHT, PieceColor.WHITE)
        );

        Move move = new Move(
                Position.fromAlgebraic("e4"),
                Position.fromAlgebraic("f6")
        );

        assertTrue(validator.isValidMove(gameState, move));
    }

    @Test
    void shouldRejectStraightMove() {
        GameState gameState = emptyGameState();
        Board board = gameState.getBoard();

        board.setPiece(
                Position.fromAlgebraic("e4"),
                new Piece(PieceType.KNIGHT, PieceColor.WHITE)
        );

        Move move = new Move(
                Position.fromAlgebraic("e4"),
                Position.fromAlgebraic("e6")
        );

        assertFalse(validator.isValidMove(gameState, move));
    }

    @Test
    void shouldAllowCapturingOpponentPiece() {
        GameState gameState = emptyGameState();
        Board board = gameState.getBoard();

        board.setPiece(
                Position.fromAlgebraic("e4"),
                new Piece(PieceType.KNIGHT, PieceColor.WHITE)
        );

        board.setPiece(
                Position.fromAlgebraic("f6"),
                new Piece(PieceType.PAWN, PieceColor.BLACK)
        );

        Move move = new Move(
                Position.fromAlgebraic("e4"),
                Position.fromAlgebraic("f6")
        );

        assertTrue(validator.isValidMove(gameState, move));
    }

    @Test
    void shouldRejectCapturingOwnPiece() {
        GameState gameState = emptyGameState();
        Board board = gameState.getBoard();

        board.setPiece(
                Position.fromAlgebraic("e4"),
                new Piece(PieceType.KNIGHT, PieceColor.WHITE)
        );

        board.setPiece(
                Position.fromAlgebraic("f6"),
                new Piece(PieceType.PAWN, PieceColor.WHITE)
        );

        Move move = new Move(
                Position.fromAlgebraic("e4"),
                Position.fromAlgebraic("f6")
        );

        assertFalse(validator.isValidMove(gameState, move));
    }

    @Test
    void shouldAllowJumpingOverPieces() {
        GameState gameState = emptyGameState();
        Board board = gameState.getBoard();
        board.setupInitialPosition();

        Move move = new Move(
                Position.fromAlgebraic("g1"),
                Position.fromAlgebraic("f3")
        );

        assertTrue(validator.isValidMove(gameState, move));
    }

    @Test
    void shouldRejectMoveWithoutKnightAtSource() {
        GameState gameState = emptyGameState();
        Board board = gameState.getBoard();

        Move move = new Move(
                Position.fromAlgebraic("e4"),
                Position.fromAlgebraic("f6")
        );

        assertFalse(validator.isValidMove(gameState, move));
    }
}
