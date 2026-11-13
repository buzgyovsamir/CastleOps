package com.castleops.chess.engine.validator.pieces;

import com.castleops.chess.game.GameState;
import com.castleops.chess.model.*;
import org.junit.jupiter.api.Test;

import static com.castleops.chess.support.GameStateFixtures.emptyGameState;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PawnMoveValidatorTest {
    private final PawnMoveValidator validator = new PawnMoveValidator();

    @Test
    void shouldAllowWhitePawnOneSquareForward() {
        GameState gameState = emptyGameState();
        Board board = gameState.getBoard();
        board.setupInitialPosition();

        Move move = new Move(
                Position.fromAlgebraic("e2"),
                Position.fromAlgebraic("e3")
        );

        assertTrue(validator.isValidMove(gameState, move));
    }

    @Test
    void shouldAllowBlackPawnOneSquareForward() {
        GameState gameState = emptyGameState();
        Board board = gameState.getBoard();
        board.setupInitialPosition();

        Move move = new Move(
                Position.fromAlgebraic("e7"),
                Position.fromAlgebraic("e6")
        );

        assertTrue(validator.isValidMove(gameState, move));
    }

    @Test
    void shouldRejectBackwardPawnMove() {
        GameState gameState = emptyGameState();
        Board board = gameState.getBoard();

        board.setPiece(
                Position.fromAlgebraic("e4"),
                new Piece(PieceType.PAWN, PieceColor.WHITE)
        );

        Move move = new Move(
                Position.fromAlgebraic("e4"),
                Position.fromAlgebraic("e3")
        );

        assertFalse(validator.isValidMove(gameState, move));
    }

    @Test
    void shouldRejectBlockedPawnMove() {
        GameState gameState = emptyGameState();
        Board board = gameState.getBoard();

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

        assertFalse(validator.isValidMove(gameState, move));
    }

    @Test
    void shouldRejectMoveWithoutPawn() {
        GameState gameState = emptyGameState();
        Board board = gameState.getBoard();

        Move move = new Move(
                Position.fromAlgebraic("e2"),
                Position.fromAlgebraic("e3")
        );

        assertFalse(validator.isValidMove(gameState, move));
    }


    @Test
    void shouldAllowWhitePawnTwoSquaresFromStartingRank() {
        GameState gameState = new GameState();

        Move move = new Move(
                Position.fromAlgebraic("e2"),
                Position.fromAlgebraic("e4")
        );

        assertTrue(validator.isValidMove(gameState, move));
    }

    @Test
    void shouldAllowBlackPawnTwoSquaresFromStartingRank() {
        GameState gameState = emptyGameState();
        Board board = gameState.getBoard();
        board.setupInitialPosition();

        Move move = new Move(
                Position.fromAlgebraic("e7"),
                Position.fromAlgebraic("e5")
        );

        assertTrue(validator.isValidMove(gameState, move));
    }

    @Test
    void shouldRejectBlockedTwoSquarePawnMove() {
        GameState gameState = emptyGameState();
        Board board = gameState.getBoard();

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

        assertFalse(validator.isValidMove(gameState, move));
    }

    @Test
    void shouldRejectTwoSquareMoveOutsideStartingRank() {
        GameState gameState = emptyGameState();
        Board board = gameState.getBoard();

        board.setPiece(
                Position.fromAlgebraic("e3"),
                new Piece(PieceType.PAWN, PieceColor.WHITE)
        );

        Move move = new Move(
                Position.fromAlgebraic("e3"),
                Position.fromAlgebraic("e5")
        );

        assertFalse(validator.isValidMove(gameState, move));
    }

    @Test
    void shouldAllowPawnDiagonalCapture() {
        GameState gameState = emptyGameState();
        Board board = gameState.getBoard();

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

        assertTrue(validator.isValidMove(gameState, move));
    }

    @Test
    void shouldRejectDiagonalMoveToEmptySquare() {
        GameState gameState = emptyGameState();
        Board board = gameState.getBoard();

        board.setPiece(
                Position.fromAlgebraic("e4"),
                new Piece(PieceType.PAWN, PieceColor.WHITE)
        );

        Move move = new Move(
                Position.fromAlgebraic("e4"),
                Position.fromAlgebraic("d5")
        );

        assertFalse(validator.isValidMove(gameState, move));
    }
}
