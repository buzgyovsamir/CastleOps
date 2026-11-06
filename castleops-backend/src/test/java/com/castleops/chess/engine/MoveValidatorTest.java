
package com.castleops.chess.engine;

import com.castleops.chess.model.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MoveValidatorTest {

    private final MoveValidator validator = new MoveValidator();

    @Test
    void shouldAllowWhitePawnOneSquareForward() {
        Board board = new Board();
        board.setupInitialPosition();

        Move move = new Move(
                Position.fromAlgebraic("e2"),
                Position.fromAlgebraic("e3")
        );

        assertTrue(validator.isValidPawnMove(board, move));
    }

    @Test
    void shouldAllowBlackPawnOneSquareForward() {
        Board board = new Board();
        board.setupInitialPosition();

        Move move = new Move(
                Position.fromAlgebraic("e7"),
                Position.fromAlgebraic("e6")
        );

        assertTrue(validator.isValidPawnMove(board, move));
    }

    @Test
    void shouldRejectBackwardPawnMove() {
        Board board = new Board();

        board.setPiece(
                Position.fromAlgebraic("e4"),
                new Piece(PieceType.PAWN, PieceColor.WHITE)
        );

        Move move = new Move(
                Position.fromAlgebraic("e4"),
                Position.fromAlgebraic("e3")
        );

        assertFalse(validator.isValidPawnMove(board, move));
    }

    @Test
    void shouldRejectBlockedPawnMove() {
        Board board = new Board();

        board.setPiece(
                Position.fromAlgebraic("e2"),
                new Piece(PieceType.PAWN, PieceColor.WHITE)
        );

        board.setPiece(
                Position.fromAlgebraic("e3"),
                new Piece(PieceType.KNIGHT, PieceColor.BLACK)
        );

        Move move = new Move(
                Position.fromAlgebraic("e2"),
                Position.fromAlgebraic("e3")
        );

        assertFalse(validator.isValidPawnMove(board, move));
    }

    @Test
    void shouldRejectTwoSquarePawnMoveForNow() {
        Board board = new Board();
        board.setupInitialPosition();

        Move move = new Move(
                Position.fromAlgebraic("e2"),
                Position.fromAlgebraic("e4")
        );

        assertFalse(validator.isValidPawnMove(board, move));
    }

    @Test
    void shouldRejectMoveWithoutPawn() {
        Board board = new Board();

        Move move = new Move(
                Position.fromAlgebraic("e2"),
                Position.fromAlgebraic("e3")
        );

        assertFalse(validator.isValidPawnMove(board, move));
    }
}
