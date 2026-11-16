package com.castleops.chess.engine;

import com.castleops.chess.model.Board;
import com.castleops.chess.model.Piece;
import com.castleops.chess.model.PieceColor;
import com.castleops.chess.model.PieceType;
import com.castleops.chess.model.Position;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InsufficientMaterialDetectorTest {

    private final InsufficientMaterialDetector detector =
            new InsufficientMaterialDetector();

    @Test
    void shouldDetectKingAgainstKing() {
        Board board = createKingsOnlyBoard();

        assertTrue(detector.hasInsufficientMaterial(board));
    }

    @ParameterizedTest
    @EnumSource(PieceColor.class)
    void shouldDetectKingAndBishopAgainstKing(PieceColor color) {
        Board board = createKingsOnlyBoard();
        place(board, "c4", PieceType.BISHOP, color);

        assertTrue(detector.hasInsufficientMaterial(board));
    }

    @ParameterizedTest
    @EnumSource(PieceColor.class)
    void shouldDetectKingAndKnightAgainstKing(PieceColor color) {
        Board board = createKingsOnlyBoard();
        place(board, "c4", PieceType.KNIGHT, color);

        assertTrue(detector.hasInsufficientMaterial(board));
    }

    @ParameterizedTest
    @EnumSource(
            value = PieceType.class,
            names = {"PAWN", "ROOK", "QUEEN"}
    )
    void shouldNotDetectDrawWithPawnRookOrQueen(PieceType type) {
        Board board = createKingsOnlyBoard();
        place(board, "a2", type, PieceColor.WHITE);

        assertFalse(detector.hasInsufficientMaterial(board));
    }

    @Test
    void shouldNotDetectDrawWithTwoKnights() {
        Board board = createKingsOnlyBoard();

        place(board, "b1", PieceType.KNIGHT, PieceColor.WHITE);
        place(board, "g1", PieceType.KNIGHT, PieceColor.WHITE);

        assertFalse(detector.hasInsufficientMaterial(board));
    }

    @Test
    void shouldNotDetectDrawWithBishopAndKnight() {
        Board board = createKingsOnlyBoard();

        place(board, "c1", PieceType.BISHOP, PieceColor.WHITE);
        place(board, "g1", PieceType.KNIGHT, PieceColor.WHITE);

        assertFalse(detector.hasInsufficientMaterial(board));
    }

    @Test
    void shouldNotDetectDrawWithOppositeColoredBishops() {
        Board board = createKingsOnlyBoard();

        place(board, "c1", PieceType.BISHOP, PieceColor.WHITE);
        place(board, "f1", PieceType.BISHOP, PieceColor.WHITE);

        assertFalse(detector.hasInsufficientMaterial(board));
    }

    @Test
    void shouldNotDetectDrawWithOneKnightForEachPlayer() {
        Board board = createKingsOnlyBoard();

        place(board, "b1", PieceType.KNIGHT, PieceColor.WHITE);
        place(board, "b8", PieceType.KNIGHT, PieceColor.BLACK);

        assertFalse(detector.hasInsufficientMaterial(board));
    }

    @Test
    void shouldNotDetectDrawInInitialPosition() {
        Board board = new Board();
        board.setupInitialPosition();

        assertFalse(detector.hasInsufficientMaterial(board));
    }

    @Test
    void shouldDetectDrawWithBishopsOnDarkSquaresForBothPlayers() {
        Board board = createKingsOnlyBoard();

        place(board, "c1", PieceType.BISHOP, PieceColor.WHITE);
        place(board, "f8", PieceType.BISHOP, PieceColor.BLACK);

        assertTrue(detector.hasInsufficientMaterial(board));
    }

    @Test
    void shouldDetectDrawWithBishopsOnLightSquaresForBothPlayers() {
        Board board = createKingsOnlyBoard();

        place(board, "f1", PieceType.BISHOP, PieceColor.WHITE);
        place(board, "c8", PieceType.BISHOP, PieceColor.BLACK);

        assertTrue(detector.hasInsufficientMaterial(board));
    }

    @Test
    void shouldDetectDrawWithMultipleBishopsOnSameSquareColor() {
        Board board = createKingsOnlyBoard();

        place(board, "c1", PieceType.BISHOP, PieceColor.WHITE);
        place(board, "e3", PieceType.BISHOP, PieceColor.WHITE);
        place(board, "f8", PieceType.BISHOP, PieceColor.BLACK);

        assertTrue(detector.hasInsufficientMaterial(board));
    }

    @Test
    void shouldDetectDrawWithSameSquareColorBishopsOwnedByOnePlayer() {
        Board board = createKingsOnlyBoard();

        place(board, "c1", PieceType.BISHOP, PieceColor.WHITE);
        place(board, "e3", PieceType.BISHOP, PieceColor.WHITE);

        assertTrue(detector.hasInsufficientMaterial(board));
    }

    @Test
    void shouldNotDetectDrawWithOppositeSquareColorBishopsForBothPlayers() {
        Board board = createKingsOnlyBoard();

        place(board, "c1", PieceType.BISHOP, PieceColor.WHITE);
        place(board, "c8", PieceType.BISHOP, PieceColor.BLACK);

        assertFalse(detector.hasInsufficientMaterial(board));
    }

    @Test
    void shouldNotDetectDrawWithBishopAgainstKnight() {
        Board board = createKingsOnlyBoard();

        place(board, "c1", PieceType.BISHOP, PieceColor.WHITE);
        place(board, "b8", PieceType.KNIGHT, PieceColor.BLACK);

        assertFalse(detector.hasInsufficientMaterial(board));
    }

    private Board createKingsOnlyBoard() {
        Board board = new Board();

        place(board, "e1", PieceType.KING, PieceColor.WHITE);
        place(board, "e8", PieceType.KING, PieceColor.BLACK);

        return board;
    }

    private void place(
            Board board,
            String square,
            PieceType type,
            PieceColor color
    ) {
        board.setPiece(
                Position.fromAlgebraic(square),
                new Piece(type, color)
        );
    }
}