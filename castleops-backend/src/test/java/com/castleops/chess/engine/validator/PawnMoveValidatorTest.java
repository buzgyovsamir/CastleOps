package com.castleops.chess.engine.validator;

import com.castleops.chess.engine.MoveValidator;
import com.castleops.chess.model.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PawnMoveValidatorTest {
    private final MoveValidator validator = new MoveValidator();

    @Test
    void shouldAllowWhitePawnOneSquareForward() {
        Board board = new Board();
        board.setupInitialPosition();

        Move move = new Move(
                Position.fromAlgebraic("e2"),
                Position.fromAlgebraic("e3")
        );

        assertTrue(validator.isValidMove(board, move));
    }

    @Test
    void shouldAllowBlackPawnOneSquareForward() {
        Board board = new Board();
        board.setupInitialPosition();

        Move move = new Move(
                Position.fromAlgebraic("e7"),
                Position.fromAlgebraic("e6")
        );

        assertTrue(validator.isValidMove(board, move));
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

        assertFalse(validator.isValidMove(board, move));
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

        assertFalse(validator.isValidMove(board, move));
    }

    @Test
    void shouldRejectMoveWithoutPawn() {
        Board board = new Board();

        Move move = new Move(
                Position.fromAlgebraic("e2"),
                Position.fromAlgebraic("e3")
        );

        assertFalse(validator.isValidMove(board, move));
    }


    @Test
    void shouldAllowWhitePawnTwoSquaresFromStartingRank() {
        Board board = new Board();
        board.setupInitialPosition();

        Move move = new Move(
                Position.fromAlgebraic("e2"),
                Position.fromAlgebraic("e4")
        );

        assertTrue(validator.isValidMove(board, move));
    }

    @Test
    void shouldAllowBlackPawnTwoSquaresFromStartingRank() {
        Board board = new Board();
        board.setupInitialPosition();

        Move move = new Move(
                Position.fromAlgebraic("e7"),
                Position.fromAlgebraic("e5")
        );

        assertTrue(validator.isValidMove(board, move));
    }

    @Test
    void shouldRejectBlockedTwoSquarePawnMove() {
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
                Position.fromAlgebraic("e4")
        );

        assertFalse(validator.isValidMove(board, move));
    }

    @Test
    void shouldRejectTwoSquareMoveOutsideStartingRank() {
        Board board = new Board();

        board.setPiece(
                Position.fromAlgebraic("e3"),
                new Piece(PieceType.PAWN, PieceColor.WHITE)
        );

        Move move = new Move(
                Position.fromAlgebraic("e3"),
                Position.fromAlgebraic("e5")
        );

        assertFalse(validator.isValidMove(board, move));
    }

    @Test
    void shouldAllowPawnDiagonalCapture() {
        Board board = new Board();

        board.setPiece(
                Position.fromAlgebraic("e4"),
                new Piece(PieceType.PAWN, PieceColor.WHITE)
        );

        board.setPiece(
                Position.fromAlgebraic("d5"),
                new Piece(PieceType.PAWN, PieceColor.BLACK)
        );

        Move move = new Move(
                Position.fromAlgebraic("e4"),
                Position.fromAlgebraic("d5")
        );

        assertTrue(validator.isValidMove(board, move));
    }

    @Test
    void shouldRejectDiagonalMoveToEmptySquare() {
        Board board = new Board();

        board.setPiece(
                Position.fromAlgebraic("e4"),
                new Piece(PieceType.PAWN, PieceColor.WHITE)
        );

        Move move = new Move(
                Position.fromAlgebraic("e4"),
                Position.fromAlgebraic("d5")
        );

        assertFalse(validator.isValidMove(board, move));
    }
}
