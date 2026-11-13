
package com.castleops.chess.engine.validator.pieces;

import com.castleops.chess.game.GameState;
import com.castleops.chess.model.*;
import org.junit.jupiter.api.Test;

import static com.castleops.chess.support.GameStateFixtures.emptyGameState;
import static org.junit.jupiter.api.Assertions.*;

class QueenMoveValidatorTest {

    private final QueenMoveValidator validator = new QueenMoveValidator();

    @Test
    void shouldAllowHorizontalMove() {
        GameState gameState = emptyGameState();
        Board board = gameState.getBoard();
        board.setPiece(
                Position.fromAlgebraic("d4"),
                new Piece(PieceType.QUEEN, PieceColor.WHITE)
        );

        Move move = new Move(
                Position.fromAlgebraic("d4"),
                Position.fromAlgebraic("a4")
        );

        assertTrue(validator.isValidMove(gameState, move));
    }

    @Test
    void shouldAllowVerticalMove() {
        GameState gameState = emptyGameState();
        Board board = gameState.getBoard();
        board.setPiece(
                Position.fromAlgebraic("d4"),
                new Piece(PieceType.QUEEN, PieceColor.WHITE)
        );

        Move move = new Move(
                Position.fromAlgebraic("d4"),
                Position.fromAlgebraic("d8")
        );

        assertTrue(validator.isValidMove(gameState, move));
    }

    @Test
    void shouldAllowDiagonalMove() {
        GameState gameState = emptyGameState();
        Board board = gameState.getBoard();
        board.setPiece(
                Position.fromAlgebraic("d4"),
                new Piece(PieceType.QUEEN, PieceColor.WHITE)
        );

        Move move = new Move(
                Position.fromAlgebraic("d4"),
                Position.fromAlgebraic("g7")
        );

        assertTrue(validator.isValidMove(gameState, move));
    }

    @Test
    void shouldRejectInvalidMovementPattern() {
        GameState gameState = emptyGameState();
        Board board = gameState.getBoard();
        board.setPiece(
                Position.fromAlgebraic("d4"),
                new Piece(PieceType.QUEEN, PieceColor.WHITE)
        );

        Move move = new Move(
                Position.fromAlgebraic("d4"),
                Position.fromAlgebraic("f5")
        );

        assertFalse(validator.isValidMove(gameState, move));
    }

    @Test
    void shouldRejectMoveWhenPathIsBlocked() {
        GameState gameState = emptyGameState();
        Board board = gameState.getBoard();
        board.setupInitialPosition();

        Move move = new Move(
                Position.fromAlgebraic("d1"),
                Position.fromAlgebraic("d4")
        );

        assertFalse(validator.isValidMove(gameState, move));
    }

    @Test
    void shouldAllowCapturingOpponentPiece() {
        GameState gameState = emptyGameState();
        Board board = gameState.getBoard();

        board.setPiece(
                Position.fromAlgebraic("d4"),
                new Piece(PieceType.QUEEN, PieceColor.WHITE)
        );

        board.setPiece(
                Position.fromAlgebraic("g7"),
                new Piece(PieceType.PAWN, PieceColor.BLACK)
        );

        Move move = new Move(
                Position.fromAlgebraic("d4"),
                Position.fromAlgebraic("g7")
        );

        assertTrue(validator.isValidMove(gameState, move));
    }

    @Test
    void shouldRejectCapturingOwnPiece() {
        GameState gameState = emptyGameState();
        Board board = gameState.getBoard();

        board.setPiece(
                Position.fromAlgebraic("d4"),
                new Piece(PieceType.QUEEN, PieceColor.WHITE)
        );

        board.setPiece(
                Position.fromAlgebraic("g7"),
                new Piece(PieceType.KNIGHT, PieceColor.WHITE)
        );

        Move move = new Move(
                Position.fromAlgebraic("d4"),
                Position.fromAlgebraic("g7")
        );

        assertFalse(validator.isValidMove(gameState, move));
    }

    @Test
    void shouldRejectMoveWithoutQueenAtSource() {
        GameState gameState = emptyGameState();
        Board board = gameState.getBoard();

        Move move = new Move(
                Position.fromAlgebraic("d4"),
                Position.fromAlgebraic("g7")
        );

        assertFalse(validator.isValidMove(gameState, move));
    }

    @Test
    void shouldAllowMoveInOppositeDirection() {
        GameState gameState = emptyGameState();
        Board board = gameState.getBoard();

        board.setPiece(
                Position.fromAlgebraic("g7"),
                new Piece(PieceType.QUEEN, PieceColor.BLACK)
        );

        Move move = new Move(
                Position.fromAlgebraic("g7"),
                Position.fromAlgebraic("d4")
        );

        assertTrue(validator.isValidMove(gameState, move));
    }
}
