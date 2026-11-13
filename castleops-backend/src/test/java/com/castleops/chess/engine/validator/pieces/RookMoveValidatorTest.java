
package com.castleops.chess.engine.validator.pieces;

import com.castleops.chess.game.GameState;
import com.castleops.chess.model.*;
import org.junit.jupiter.api.Test;

import static com.castleops.chess.support.GameStateFixtures.emptyGameState;
import static org.junit.jupiter.api.Assertions.*;

class RookMoveValidatorTest {

    private final RookMoveValidator validator = new RookMoveValidator();

    @Test
    void shouldAllowVerticalMove() {
        GameState gameState = emptyGameState();
        Board board = gameState.getBoard();
        board.setPiece(
                Position.fromAlgebraic("a1"),
                new Piece(PieceType.ROOK, PieceColor.WHITE)
        );

        Move move = new Move(
                Position.fromAlgebraic("a1"),
                Position.fromAlgebraic("a5")
        );

        assertTrue(validator.isValidMove(gameState, move));
    }

    @Test
    void shouldAllowHorizontalMove() {
        GameState gameState = emptyGameState();
        Board board = gameState.getBoard();
        board.setPiece(
                Position.fromAlgebraic("a1"),
                new Piece(PieceType.ROOK, PieceColor.WHITE)
        );

        Move move = new Move(
                Position.fromAlgebraic("a1"),
                Position.fromAlgebraic("e1")
        );

        assertTrue(validator.isValidMove(gameState, move));
    }

    @Test
    void shouldRejectDiagonalMove() {
        GameState gameState = emptyGameState();
        Board board = gameState.getBoard();
        board.setPiece(
                Position.fromAlgebraic("a1"),
                new Piece(PieceType.ROOK, PieceColor.WHITE)
        );

        Move move = new Move(
                Position.fromAlgebraic("a1"),
                Position.fromAlgebraic("d4")
        );

        assertFalse(validator.isValidMove(gameState, move));
    }

    @Test
    void shouldRejectMoveWhenPathIsBlocked() {
        GameState gameState = emptyGameState();
        Board board = gameState.getBoard();
        board.setupInitialPosition();

        Move move = new Move(
                Position.fromAlgebraic("a1"),
                Position.fromAlgebraic("a5")
        );

        assertFalse(validator.isValidMove(gameState, move));
    }

    @Test
    void shouldAllowCapturingOpponentPiece() {
        GameState gameState = emptyGameState();
        Board board = gameState.getBoard();

        board.setPiece(
                Position.fromAlgebraic("a1"),
                new Piece(PieceType.ROOK, PieceColor.WHITE)
        );

        board.setPiece(
                Position.fromAlgebraic("a5"),
                new Piece(PieceType.PAWN, PieceColor.BLACK)
        );

        Move move = new Move(
                Position.fromAlgebraic("a1"),
                Position.fromAlgebraic("a5")
        );

        assertTrue(validator.isValidMove(gameState, move));
    }

    @Test
    void shouldRejectCapturingOwnPiece() {
        GameState gameState = emptyGameState();
        Board board = gameState.getBoard();

        board.setPiece(
                Position.fromAlgebraic("a1"),
                new Piece(PieceType.ROOK, PieceColor.WHITE)
        );

        board.setPiece(
                Position.fromAlgebraic("a5"),
                new Piece(PieceType.PAWN, PieceColor.WHITE)
        );

        Move move = new Move(
                Position.fromAlgebraic("a1"),
                Position.fromAlgebraic("a5")
        );

        assertFalse(validator.isValidMove(gameState, move));
    }

    @Test
    void shouldRejectMoveWithoutRookAtSource() {
        GameState gameState = emptyGameState();
        Board board = gameState.getBoard();

        Move move = new Move(
                Position.fromAlgebraic("a1"),
                Position.fromAlgebraic("a5")
        );

        assertFalse(validator.isValidMove(gameState, move));
    }

    @Test
    void shouldAllowMoveInOppositeDirection() {
        GameState gameState = emptyGameState();
        Board board = gameState.getBoard();

        board.setPiece(
                Position.fromAlgebraic("a5"),
                new Piece(PieceType.ROOK, PieceColor.BLACK)
        );

        Move move = new Move(
                Position.fromAlgebraic("a5"),
                Position.fromAlgebraic("a1")
        );

        assertTrue(validator.isValidMove(gameState, move));
    }
}
