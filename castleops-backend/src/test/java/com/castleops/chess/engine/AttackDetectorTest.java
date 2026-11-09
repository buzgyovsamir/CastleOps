
package com.castleops.chess.engine;

import com.castleops.chess.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AttackDetectorTest {

    private Board board;
    private AttackDetector attackDetector;

    @BeforeEach
    void setUp() {
        board = new Board();
        attackDetector = new AttackDetector();
    }

    @Test
    void shouldDetectWhitePawnAttackOnEmptySquare() {
        placePiece("e4", PieceType.PAWN, PieceColor.WHITE);

        assertTrue(isAttacked("d5", PieceColor.WHITE));
        assertTrue(isAttacked("f5", PieceColor.WHITE));
        assertFalse(isAttacked("e5", PieceColor.WHITE));
    }

    @Test
    void shouldDetectBlackPawnAttack() {
        placePiece("e5", PieceType.PAWN, PieceColor.BLACK);

        assertTrue(isAttacked("d4", PieceColor.BLACK));
        assertTrue(isAttacked("f4", PieceColor.BLACK));
        assertFalse(isAttacked("e4", PieceColor.BLACK));
    }

    @Test
    void shouldDetectKnightAttack() {
        placePiece("e4", PieceType.KNIGHT, PieceColor.WHITE);

        assertTrue(isAttacked("f6", PieceColor.WHITE));
        assertTrue(isAttacked("c5", PieceColor.WHITE));
        assertFalse(isAttacked("e5", PieceColor.WHITE));
    }

    @Test
    void shouldDetectBishopAttack() {
        placePiece("c1", PieceType.BISHOP, PieceColor.WHITE);

        assertTrue(isAttacked("h6", PieceColor.WHITE));
        assertFalse(isAttacked("c6", PieceColor.WHITE));
    }

    @Test
    void shouldNotDetectAttackThroughBlockingPiece() {
        placePiece("c1", PieceType.BISHOP, PieceColor.WHITE);
        placePiece("e3", PieceType.ROOK, PieceColor.WHITE);

        assertTrue(isAttacked("e3", PieceColor.WHITE));
        assertFalse(isAttacked("f4", PieceColor.WHITE));
    }

    @Test
    void shouldDetectRookAttack() {
        placePiece("a1", PieceType.ROOK, PieceColor.WHITE);

        assertTrue(isAttacked("a8", PieceColor.WHITE));
        assertTrue(isAttacked("h1", PieceColor.WHITE));
        assertFalse(isAttacked("h8", PieceColor.WHITE));
    }

    @Test
    void shouldDetectQueenAttack() {
        placePiece("d4", PieceType.QUEEN, PieceColor.BLACK);

        assertTrue(isAttacked("d8", PieceColor.BLACK));
        assertTrue(isAttacked("h4", PieceColor.BLACK));
        assertTrue(isAttacked("g7", PieceColor.BLACK));
    }

    @Test
    void shouldDetectKingAttack() {
        placePiece("e4", PieceType.KING, PieceColor.WHITE);

        assertTrue(isAttacked("d5", PieceColor.WHITE));
        assertTrue(isAttacked("e5", PieceColor.WHITE));
        assertFalse(isAttacked("e6", PieceColor.WHITE));
    }

    @Test
    void shouldOnlyConsiderAttackingColor() {
        placePiece("a1", PieceType.ROOK, PieceColor.WHITE);

        assertTrue(isAttacked("a8", PieceColor.WHITE));
        assertFalse(isAttacked("a8", PieceColor.BLACK));
    }

    @Test
    void shouldNotDetectAttackFromEmptyBoard() {
        assertFalse(isAttacked("e4", PieceColor.WHITE));
        assertFalse(isAttacked("e4", PieceColor.BLACK));
    }

    private void placePiece(
            String square,
            PieceType type,
            PieceColor color) {

        board.setPiece(
                Position.fromAlgebraic(square),
                new Piece(type, color)
        );
    }

    private boolean isAttacked(
            String square,
            PieceColor attackingColor) {

        return attackDetector.isSquareAttacked(
                board,
                Position.fromAlgebraic(square),
                attackingColor
        );
    }

    @Test
    void shouldDetectAttackFromPinnedPiece() {
        placePiece("e1", PieceType.KING, PieceColor.WHITE);
        placePiece("e2", PieceType.ROOK, PieceColor.WHITE);
        placePiece("e8", PieceType.ROOK, PieceColor.BLACK);

        assertTrue(isAttacked("a2", PieceColor.WHITE));
    }
}
