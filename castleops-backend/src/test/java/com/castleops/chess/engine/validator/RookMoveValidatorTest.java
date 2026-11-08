
package com.castleops.chess.engine.validator;

import com.castleops.chess.model.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RookMoveValidatorTest {

    private final RookMoveValidator validator = new RookMoveValidator();

    @Test
    void shouldAllowVerticalMove() {
        Board board = new Board();
        board.setPiece(
                Position.fromAlgebraic("a1"),
                new Piece(PieceType.ROOK, PieceColor.WHITE)
        );

        Move move = new Move(
                Position.fromAlgebraic("a1"),
                Position.fromAlgebraic("a5")
        );

        assertTrue(validator.isValidMove(board, move));
    }

    @Test
    void shouldAllowHorizontalMove() {
        Board board = new Board();
        board.setPiece(
                Position.fromAlgebraic("a1"),
                new Piece(PieceType.ROOK, PieceColor.WHITE)
        );

        Move move = new Move(
                Position.fromAlgebraic("a1"),
                Position.fromAlgebraic("e1")
        );

        assertTrue(validator.isValidMove(board, move));
    }

    @Test
    void shouldRejectDiagonalMove() {
        Board board = new Board();
        board.setPiece(
                Position.fromAlgebraic("a1"),
                new Piece(PieceType.ROOK, PieceColor.WHITE)
        );

        Move move = new Move(
                Position.fromAlgebraic("a1"),
                Position.fromAlgebraic("d4")
        );

        assertFalse(validator.isValidMove(board, move));
    }

    @Test
    void shouldRejectMoveWhenPathIsBlocked() {
        Board board = new Board();
        board.setupInitialPosition();

        Move move = new Move(
                Position.fromAlgebraic("a1"),
                Position.fromAlgebraic("a5")
        );

        assertFalse(validator.isValidMove(board, move));
    }

    @Test
    void shouldAllowCapturingOpponentPiece() {
        Board board = new Board();

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

        assertTrue(validator.isValidMove(board, move));
    }

    @Test
    void shouldRejectCapturingOwnPiece() {
        Board board = new Board();

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

        assertFalse(validator.isValidMove(board, move));
    }

    @Test
    void shouldRejectMoveWithoutRookAtSource() {
        Board board = new Board();

        Move move = new Move(
                Position.fromAlgebraic("a1"),
                Position.fromAlgebraic("a5")
        );

        assertFalse(validator.isValidMove(board, move));
    }

    @Test
    void shouldAllowMoveInOppositeDirection() {
        Board board = new Board();

        board.setPiece(
                Position.fromAlgebraic("a5"),
                new Piece(PieceType.ROOK, PieceColor.BLACK)
        );

        Move move = new Move(
                Position.fromAlgebraic("a5"),
                Position.fromAlgebraic("a1")
        );

        assertTrue(validator.isValidMove(board, move));
    }
}
