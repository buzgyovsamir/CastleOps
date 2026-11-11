
package com.castleops.chess.engine.validator.pieces;

import com.castleops.chess.model.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KnightMoveValidatorTest {

    private final KnightMoveValidator validator = new KnightMoveValidator();

    @Test
    void shouldAllowValidLShapedMove() {
        Board board = new Board();

        board.setPiece(
                Position.fromAlgebraic("e4"),
                new Piece(PieceType.KNIGHT, PieceColor.WHITE)
        );

        Move move = new Move(
                Position.fromAlgebraic("e4"),
                Position.fromAlgebraic("f6")
        );

        assertTrue(validator.isValidMove(board, move));
    }

    @Test
    void shouldRejectStraightMove() {
        Board board = new Board();

        board.setPiece(
                Position.fromAlgebraic("e4"),
                new Piece(PieceType.KNIGHT, PieceColor.WHITE)
        );

        Move move = new Move(
                Position.fromAlgebraic("e4"),
                Position.fromAlgebraic("e6")
        );

        assertFalse(validator.isValidMove(board, move));
    }

    @Test
    void shouldAllowCapturingOpponentPiece() {
        Board board = new Board();

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

        assertTrue(validator.isValidMove(board, move));
    }

    @Test
    void shouldRejectCapturingOwnPiece() {
        Board board = new Board();

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

        assertFalse(validator.isValidMove(board, move));
    }

    @Test
    void shouldAllowJumpingOverPieces() {
        Board board = new Board();
        board.setupInitialPosition();

        Move move = new Move(
                Position.fromAlgebraic("g1"),
                Position.fromAlgebraic("f3")
        );

        assertTrue(validator.isValidMove(board, move));
    }

    @Test
    void shouldRejectMoveWithoutKnightAtSource() {
        Board board = new Board();

        Move move = new Move(
                Position.fromAlgebraic("e4"),
                Position.fromAlgebraic("f6")
        );

        assertFalse(validator.isValidMove(board, move));
    }
}
