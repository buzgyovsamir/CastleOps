
package com.castleops.chess.engine.validator.pieces;

import com.castleops.chess.model.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KingMoveValidatorTest {

    private final KingMoveValidator validator = new KingMoveValidator();

    @Test
    void shouldAllowVerticalMove() {
        Board board = new Board();

        board.setPiece(
                Position.fromAlgebraic("e4"),
                new Piece(PieceType.KING, PieceColor.WHITE)
        );

        Move move = new Move(
                Position.fromAlgebraic("e4"),
                Position.fromAlgebraic("e5")
        );

        assertTrue(validator.isValidMove(board, move));
    }

    @Test
    void shouldAllowHorizontalMove() {
        Board board = new Board();

        board.setPiece(
                Position.fromAlgebraic("e4"),
                new Piece(PieceType.KING, PieceColor.WHITE)
        );

        Move move = new Move(
                Position.fromAlgebraic("e4"),
                Position.fromAlgebraic("f4")
        );

        assertTrue(validator.isValidMove(board, move));
    }

    @Test
    void shouldAllowDiagonalMove() {
        Board board = new Board();

        board.setPiece(
                Position.fromAlgebraic("e4"),
                new Piece(PieceType.KING, PieceColor.WHITE)
        );

        Move move = new Move(
                Position.fromAlgebraic("e4"),
                Position.fromAlgebraic("f5")
        );

        assertTrue(validator.isValidMove(board, move));
    }

    @Test
    void shouldRejectMovingTwoSquaresVertically() {
        Board board = new Board();

        board.setPiece(
                Position.fromAlgebraic("e4"),
                new Piece(PieceType.KING, PieceColor.WHITE)
        );

        Move move = new Move(
                Position.fromAlgebraic("e4"),
                Position.fromAlgebraic("e6")
        );

        assertFalse(validator.isValidMove(board, move));
    }

    @Test
    void shouldRejectMovingTwoSquaresDiagonally() {
        Board board = new Board();

        board.setPiece(
                Position.fromAlgebraic("e4"),
                new Piece(PieceType.KING, PieceColor.WHITE)
        );

        Move move = new Move(
                Position.fromAlgebraic("e4"),
                Position.fromAlgebraic("g6")
        );

        assertFalse(validator.isValidMove(board, move));
    }

    @Test
    void shouldAllowCapturingOpponentPiece() {
        Board board = new Board();

        board.setPiece(
                Position.fromAlgebraic("e4"),
                new Piece(PieceType.KING, PieceColor.WHITE)
        );

        board.setPiece(
                Position.fromAlgebraic("f5"),
                new Piece(PieceType.PAWN, PieceColor.BLACK)
        );

        Move move = new Move(
                Position.fromAlgebraic("e4"),
                Position.fromAlgebraic("f5")
        );

        assertTrue(validator.isValidMove(board, move));
    }

    @Test
    void shouldRejectCapturingOwnPiece() {
        Board board = new Board();

        board.setPiece(
                Position.fromAlgebraic("e4"),
                new Piece(PieceType.KING, PieceColor.WHITE)
        );

        board.setPiece(
                Position.fromAlgebraic("f5"),
                new Piece(PieceType.PAWN, PieceColor.WHITE)
        );

        Move move = new Move(
                Position.fromAlgebraic("e4"),
                Position.fromAlgebraic("f5")
        );

        assertFalse(validator.isValidMove(board, move));
    }

    @Test
    void shouldRejectMoveWithoutKingAtSource() {
        Board board = new Board();

        Move move = new Move(
                Position.fromAlgebraic("e4"),
                Position.fromAlgebraic("e5")
        );

        assertFalse(validator.isValidMove(board, move));
    }

    @Test
    void shouldAllowMoveInOppositeDirection() {
        Board board = new Board();

        board.setPiece(
                Position.fromAlgebraic("e5"),
                new Piece(PieceType.KING, PieceColor.BLACK)
        );

        Move move = new Move(
                Position.fromAlgebraic("e5"),
                Position.fromAlgebraic("d4")
        );

        assertTrue(validator.isValidMove(board, move));
    }
}
