
package com.castleops.chess.engine.validator;

import com.castleops.chess.model.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QueenMoveValidatorTest {

    private final QueenMoveValidator validator = new QueenMoveValidator();

    @Test
    void shouldAllowHorizontalMove() {
        Board board = new Board();
        board.setPiece(
                Position.fromAlgebraic("d4"),
                new Piece(PieceType.QUEEN, PieceColor.WHITE)
        );

        Move move = new Move(
                Position.fromAlgebraic("d4"),
                Position.fromAlgebraic("a4")
        );

        assertTrue(validator.isValidMove(board, move));
    }

    @Test
    void shouldAllowVerticalMove() {
        Board board = new Board();
        board.setPiece(
                Position.fromAlgebraic("d4"),
                new Piece(PieceType.QUEEN, PieceColor.WHITE)
        );

        Move move = new Move(
                Position.fromAlgebraic("d4"),
                Position.fromAlgebraic("d8")
        );

        assertTrue(validator.isValidMove(board, move));
    }

    @Test
    void shouldAllowDiagonalMove() {
        Board board = new Board();
        board.setPiece(
                Position.fromAlgebraic("d4"),
                new Piece(PieceType.QUEEN, PieceColor.WHITE)
        );

        Move move = new Move(
                Position.fromAlgebraic("d4"),
                Position.fromAlgebraic("g7")
        );

        assertTrue(validator.isValidMove(board, move));
    }

    @Test
    void shouldRejectInvalidMovementPattern() {
        Board board = new Board();
        board.setPiece(
                Position.fromAlgebraic("d4"),
                new Piece(PieceType.QUEEN, PieceColor.WHITE)
        );

        Move move = new Move(
                Position.fromAlgebraic("d4"),
                Position.fromAlgebraic("f5")
        );

        assertFalse(validator.isValidMove(board, move));
    }

    @Test
    void shouldRejectMoveWhenPathIsBlocked() {
        Board board = new Board();
        board.setupInitialPosition();

        Move move = new Move(
                Position.fromAlgebraic("d1"),
                Position.fromAlgebraic("d4")
        );

        assertFalse(validator.isValidMove(board, move));
    }

    @Test
    void shouldAllowCapturingOpponentPiece() {
        Board board = new Board();

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

        assertTrue(validator.isValidMove(board, move));
    }

    @Test
    void shouldRejectCapturingOwnPiece() {
        Board board = new Board();

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

        assertFalse(validator.isValidMove(board, move));
    }

    @Test
    void shouldRejectMoveWithoutQueenAtSource() {
        Board board = new Board();

        Move move = new Move(
                Position.fromAlgebraic("d4"),
                Position.fromAlgebraic("g7")
        );

        assertFalse(validator.isValidMove(board, move));
    }

    @Test
    void shouldAllowMoveInOppositeDirection() {
        Board board = new Board();

        board.setPiece(
                Position.fromAlgebraic("g7"),
                new Piece(PieceType.QUEEN, PieceColor.BLACK)
        );

        Move move = new Move(
                Position.fromAlgebraic("g7"),
                Position.fromAlgebraic("d4")
        );

        assertTrue(validator.isValidMove(board, move));
    }
}
