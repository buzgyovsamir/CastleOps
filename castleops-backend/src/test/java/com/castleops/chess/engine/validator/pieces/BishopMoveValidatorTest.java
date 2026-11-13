
package com.castleops.chess.engine.validator.pieces;

import com.castleops.chess.game.GameState;
import com.castleops.chess.model.*;
import org.junit.jupiter.api.Test;

import static com.castleops.chess.support.GameStateFixtures.emptyGameState;
import static org.junit.jupiter.api.Assertions.*;

class BishopMoveValidatorTest {

    private final BishopMoveValidator validator = new BishopMoveValidator();

    @Test
    void shouldAllowDiagonalMoveOnEmptyBoard() {
        GameState gameState = emptyGameState();
        Board board = gameState.getBoard();

        board.setPiece(
                Position.fromAlgebraic("c1"),
                new Piece(PieceType.BISHOP, PieceColor.WHITE)
        );

        Move move = new Move(
                Position.fromAlgebraic("c1"),
                Position.fromAlgebraic("f4")
        );

        assertTrue(validator.isValidMove(gameState, move));
    }

    @Test
    void shouldRejectHorizontalMove() {
        GameState gameState = emptyGameState();
        Board board = gameState.getBoard();

        board.setPiece(
                Position.fromAlgebraic("c1"),
                new Piece(PieceType.BISHOP, PieceColor.WHITE)
        );

        Move move = new Move(
                Position.fromAlgebraic("c1"),
                Position.fromAlgebraic("f1")
        );

        assertFalse(validator.isValidMove(gameState, move));
    }

    @Test
    void shouldRejectVerticalMove() {
        GameState gameState = emptyGameState();
        Board board = gameState.getBoard();

        board.setPiece(
                Position.fromAlgebraic("c1"),
                new Piece(PieceType.BISHOP, PieceColor.WHITE)
        );

        Move move = new Move(
                Position.fromAlgebraic("c1"),
                Position.fromAlgebraic("c4")
        );

        assertFalse(validator.isValidMove(gameState, move));
    }

    @Test
    void shouldRejectMoveWhenPathIsBlocked() {
        GameState gameState = emptyGameState();
        Board board = gameState.getBoard();
        board.setupInitialPosition();

        Move move = new Move(
                Position.fromAlgebraic("c1"),
                Position.fromAlgebraic("f4")
        );

        assertFalse(validator.isValidMove(gameState, move));
    }

    @Test
    void shouldAllowCapturingOpponentPiece() {
        GameState gameState = emptyGameState();
        Board board = gameState.getBoard();

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

        assertTrue(validator.isValidMove(gameState, move));
    }

    @Test
    void shouldRejectCapturingOwnPiece() {
        GameState gameState = emptyGameState();
        Board board = gameState.getBoard();

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

        assertFalse(validator.isValidMove(gameState, move));
    }

    @Test
    void shouldRejectMoveWithoutBishopAtSource() {
        GameState gameState = emptyGameState();
        Board board = gameState.getBoard();

        Move move = new Move(
                Position.fromAlgebraic("c1"),
                Position.fromAlgebraic("f4")
        );

        assertFalse(validator.isValidMove(gameState, move));
    }

    @Test
    void shouldAllowDiagonalMoveInOppositeDirection() {
        GameState gameState = emptyGameState();
        Board board = gameState.getBoard();

        board.setPiece(
                Position.fromAlgebraic("f4"),
                new Piece(PieceType.BISHOP, PieceColor.BLACK)
        );

        Move move = new Move(
                Position.fromAlgebraic("f4"),
                Position.fromAlgebraic("c1")
        );

        assertTrue(validator.isValidMove(gameState, move));
    }
}
