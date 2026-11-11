
package com.castleops.chess.engine;

import com.castleops.chess.model.*;
import com.castleops.chess.game.GameState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CheckDetectorTest {

    private Board board;
    private GameState gameState;
    private CheckDetector checkDetector;

    @BeforeEach
    void setUp() {
        gameState = new GameState();
        board = gameState.getBoard();
        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                board.setPiece(new Position(file, rank), null);
            }
        }
        checkDetector = new CheckDetector();
    }

    @Test
    void shouldNotDetectCheckInInitialPosition() {
        board.setupInitialPosition();

        assertFalse(checkDetector.isInCheck(gameState, PieceColor.WHITE));
        assertFalse(checkDetector.isInCheck(gameState, PieceColor.BLACK));
    }

    @Test
    void shouldDetectCheckFromRook() {
        placePiece("e1", PieceType.KING, PieceColor.WHITE);
        placePiece("e8", PieceType.ROOK, PieceColor.BLACK);

        assertTrue(checkDetector.isInCheck(gameState, PieceColor.WHITE));
    }

    @Test
    void shouldDetectCheckFromBishop() {
        placePiece("e1", PieceType.KING, PieceColor.WHITE);
        placePiece("h4", PieceType.BISHOP, PieceColor.BLACK);

        assertTrue(checkDetector.isInCheck(gameState, PieceColor.WHITE));
    }

    @Test
    void shouldDetectCheckFromKnight() {
        placePiece("e1", PieceType.KING, PieceColor.WHITE);
        placePiece("f3", PieceType.KNIGHT, PieceColor.BLACK);

        assertTrue(checkDetector.isInCheck(gameState, PieceColor.WHITE));
    }

    @Test
    void shouldDetectCheckFromPawn() {
        placePiece("e4", PieceType.KING, PieceColor.WHITE);
        placePiece("d5", PieceType.PAWN, PieceColor.BLACK);

        assertTrue(checkDetector.isInCheck(gameState, PieceColor.WHITE));
    }

    @Test
    void shouldNotDetectCheckThroughBlockingPiece() {
        placePiece("e1", PieceType.KING, PieceColor.WHITE);
        placePiece("e8", PieceType.ROOK, PieceColor.BLACK);
        placePiece("e4", PieceType.PAWN, PieceColor.WHITE);

        assertFalse(checkDetector.isInCheck(gameState, PieceColor.WHITE));
    }

    @Test
    void shouldDetectCheckAgainstBlackKing() {
        placePiece("e8", PieceType.KING, PieceColor.BLACK);
        placePiece("e1", PieceType.ROOK, PieceColor.WHITE);

        assertTrue(checkDetector.isInCheck(gameState, PieceColor.BLACK));
    }

    private void placePiece(
            String square,
            PieceType type,
            PieceColor color) {

        if (type == PieceType.KING) {
            gameState.updateKingPosition(color, Position.fromAlgebraic(square));
        }
        board.setPiece(
                Position.fromAlgebraic(square),
                new Piece(type, color)
        );
    }
}
