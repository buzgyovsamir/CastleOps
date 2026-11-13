package com.castleops.chess.engine.validator.pieces;

import com.castleops.chess.engine.validator.EnPassantValidator;
import com.castleops.chess.game.GameState;
import com.castleops.chess.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PawnEnPassantTest {

    private GameState gameState;
    private Board board;
    private PawnMoveValidator validator;

    @BeforeEach
    void setUp() {
        gameState = new GameState();
        board = gameState.getBoard();
        validator = new PawnMoveValidator();

        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                board.setPiece(new Position(file, rank), null);
            }
        }
    }

    @Test
    void shouldAllowWhiteEnPassant() {
        placePiece("e5", PieceType.PAWN, PieceColor.WHITE);
        placePiece("d5", PieceType.PAWN, PieceColor.BLACK);
        gameState.updateEnPassantTarget(position("d6"));

        assertTrue(validator.isValidMove(
                gameState, move("e5", "d6")
        ));
    }

    @Test
    void shouldAllowBlackEnPassant() {
        placePiece("d4", PieceType.PAWN, PieceColor.BLACK);
        placePiece("e4", PieceType.PAWN, PieceColor.WHITE);
        gameState.updateEnPassantTarget(position("e3"));
        gameState.switchTurn();

        assertTrue(validator.isValidMove(
                gameState, move("d4", "e3")
        ));
    }

    @Test
    void shouldRejectWithoutEnPassantTarget() {
        placePiece("e5", PieceType.PAWN, PieceColor.WHITE);
        placePiece("d5", PieceType.PAWN, PieceColor.BLACK);

        assertFalse(validator.isValidMove(
                gameState, move("e5", "d6")
        ));
    }

    @Test
    void shouldRejectDifferentTarget() {
        placePiece("e5", PieceType.PAWN, PieceColor.WHITE);
        placePiece("d5", PieceType.PAWN, PieceColor.BLACK);
        gameState.updateEnPassantTarget(position("f6"));

        assertFalse(validator.isValidMove(
                gameState, move("e5", "d6")
        ));
    }

    @Test
    void shouldRejectWhenCapturedPawnIsMissing() {
        placePiece("e5", PieceType.PAWN, PieceColor.WHITE);
        gameState.updateEnPassantTarget(position("d6"));

        assertFalse(validator.isValidMove(
                gameState, move("e5", "d6")
        ));
    }

    @Test
    void shouldRejectCapturingOwnPawn() {
        placePiece("e5", PieceType.PAWN, PieceColor.WHITE);
        placePiece("d5", PieceType.PAWN, PieceColor.WHITE);
        gameState.updateEnPassantTarget(position("d6"));

        assertFalse(validator.isValidMove(
                gameState, move("e5", "d6")
        ));
    }

    @Test
    void shouldRejectCapturingAnotherPieceType() {
        placePiece("e5", PieceType.PAWN, PieceColor.WHITE);
        placePiece("d5", PieceType.ROOK, PieceColor.BLACK);
        gameState.updateEnPassantTarget(position("d6"));

        assertFalse(validator.isValidMove(
                gameState, move("e5", "d6")
        ));
    }

    @Test
    void shouldAllowOrdinaryCaptureOnOccupiedDestination() {
        placePiece("e5", PieceType.PAWN, PieceColor.WHITE);
        placePiece("d5", PieceType.PAWN, PieceColor.BLACK);
        placePiece("d6", PieceType.KNIGHT, PieceColor.BLACK);
        gameState.updateEnPassantTarget(position("d6"));

        assertTrue(validator.isValidMove(
                gameState, move("e5", "d6")
        ));
    }

    @Test
    void shouldRejectNonPawnMove() {
        placePiece("e5", PieceType.BISHOP, PieceColor.WHITE);
        placePiece("d5", PieceType.PAWN, PieceColor.BLACK);
        gameState.updateEnPassantTarget(position("d6"));

        assertFalse(validator.isValidMove(
                gameState, move("e5", "d6")
        ));
    }

    @Test
    void shouldRejectWrongStartingRank() {
        placePiece("e4", PieceType.PAWN, PieceColor.WHITE);
        placePiece("d4", PieceType.PAWN, PieceColor.BLACK);
        gameState.updateEnPassantTarget(position("d5"));

        assertFalse(validator.isValidMove(
                gameState, move("e4", "d5")
        ));
    }

    @Test
    void shouldRejectBackwardCapture() {
        placePiece("e5", PieceType.PAWN, PieceColor.WHITE);
        placePiece("d5", PieceType.PAWN, PieceColor.BLACK);
        gameState.updateEnPassantTarget(position("d4"));

        assertFalse(validator.isValidMove(
                gameState, move("e5", "d4")
        ));
    }

    @Test
    void shouldRejectAfterTargetIsCleared() {
        placePiece("e5", PieceType.PAWN, PieceColor.WHITE);
        placePiece("d5", PieceType.PAWN, PieceColor.BLACK);
        gameState.updateEnPassantTarget(position("d6"));

        assertTrue(validator.isValidMove(
                gameState, move("e5", "d6")
        ));

        gameState.updateEnPassantTarget(null);

        assertFalse(validator.isValidMove(
                gameState, move("e5", "d6")
        ));
    }

    private void placePiece(
            String square,
            PieceType type,
            PieceColor color) {

        board.setPiece(position(square), new Piece(type, color));
    }

    private Position position(String square) {
        return Position.fromAlgebraic(square);
    }

    private Move move(String from, String to) {
        return new Move(position(from), position(to));
    }
}