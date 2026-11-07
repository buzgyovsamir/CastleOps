
package com.castleops.chess.engine.validator;

import com.castleops.chess.model.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BishopMoveValidatorTest {

    private final BishopMoveValidator validator = new BishopMoveValidator();

    @Test
    void shouldAllowDiagonalMoveOnEmptyBoard() {
        Board board = new Board();

        board.setPiece(
                Position.fromAlgebraic("c1"),
                new Piece(PieceType.BISHOP, PieceColor.WHITE)
        );

        Move move = new Move(
                Position.fromAlgebraic("c1"),
                Position.fromAlgebraic("f4")
        );

        assertTrue(validator.isValidMove(board, move));
    }

    @Test
    void shouldRejectHorizontalMove() {
        Board board = new Board();

        board.setPiece(
                Position.fromAlgebraic("c1"),
                new Piece(PieceType.BISHOP, PieceColor.WHITE)
        );

        Move move = new Move(
                Position.fromAlgebraic("c1"),
                Position.fromAlgebraic("f1")
        );

        assertFalse(validator.isValidMove(board, move));
    }

    @Test
    void shouldRejectVerticalMove() {
        Board board = new Board();

        board.setPiece(
                Position.fromAlgebraic("c1"),
                new Piece(PieceType.BISHOP, PieceColor.WHITE)
        );

        Move move = new Move(
                Position.fromAlgebraic("c1"),
                Position.fromAlgebraic("c4")
        );

        assertFalse(validator.isValidMove(board, move));
    }

    @Test
    void shouldRejectMoveWhenPathIsBlocked() {
        Board board = new Board();
        board.setupInitialPosition();

        Move move = new Move(
                Position.fromAlgebraic("c1"),
                Position.fromAlgebraic("f4")
        );

        assertFalse(validator.isValidMove(board, move));
    }

    @Test
    void shouldAllowCapturingOpponentPiece() {
        Board board = new Board();

        board.setPiece(
                Position.fromAlgebraic("c1"),
                new Piece(PieceType.BISHOP, PieceColor.WHITE)
        );

        board.setPiece(
                Position.fromAlgebraic("f4"),
                new Piece(PieceType.PAWN, PieceColor.BLACK)
        );

        Move move = new Move(
                Position.fromAlgebraic("c1"),
                Position.fromAlgebraic("f4")
        );

        assertTrue(validator.isValidMove(board, move));
    }

    @Test
    void shouldRejectCapturingOwnPiece() {
        Board board = new Board();

        board.setPiece(
                Position.fromAlgebraic("c1"),
                new Piece(PieceType.BISHOP, PieceColor.WHITE)
        );

        board.setPiece(
                Position.fromAlgebraic("f4"),
                new Piece(PieceType.KNIGHT, PieceColor.WHITE)
        );

        Move move = new Move(
                Position.fromAlgebraic("c1"),
                Position.fromAlgebraic("f4")
        );

        assertFalse(validator.isValidMove(board, move));
    }

    @Test
    void shouldRejectMoveWithoutBishopAtSource() {
        Board board = new Board();

        Move move = new Move(
                Position.fromAlgebraic("c1"),
                Position.fromAlgebraic("f4")
        );

        assertFalse(validator.isValidMove(board, move));
    }

    @Test
    void shouldAllowDiagonalMoveInOppositeDirection() {
        Board board = new Board();

        board.setPiece(
                Position.fromAlgebraic("f4"),
                new Piece(PieceType.BISHOP, PieceColor.BLACK)
        );

        Move move = new Move(
                Position.fromAlgebraic("f4"),
                Position.fromAlgebraic("c1")
        );

        assertTrue(validator.isValidMove(board, move));
    }
}
