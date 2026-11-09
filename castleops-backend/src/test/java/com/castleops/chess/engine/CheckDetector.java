//
//package com.castleops.chess.engine;
//
//import com.castleops.chess.model.*;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class CheckDetectorTest {
//
//    private Board board;
//    private CheckDetector checkDetector;
//
//    @BeforeEach
//    void setUp() {
//        board = new Board();
//        checkDetector = new CheckDetector();
//    }
//
//    @Test
//    void shouldNotDetectCheckInInitialPosition() {
//        board.setupInitialPosition();
//
//        assertFalse(checkDetector.isInCheck(board, PieceColor.WHITE));
//        assertFalse(checkDetector.isInCheck(board, PieceColor.BLACK));
//    }
//
//    @Test
//    void shouldDetectCheckFromRook() {
//        placePiece("e1", PieceType.KING, PieceColor.WHITE);
//        placePiece("e8", PieceType.ROOK, PieceColor.BLACK);
//
//        assertTrue(checkDetector.isInCheck(board, PieceColor.WHITE));
//    }
//
//    @Test
//    void shouldDetectCheckFromBishop() {
//        placePiece("e1", PieceType.KING, PieceColor.WHITE);
//        placePiece("h4", PieceType.BISHOP, PieceColor.BLACK);
//
//        assertTrue(checkDetector.isInCheck(board, PieceColor.WHITE));
//    }
//
//    @Test
//    void shouldDetectCheckFromKnight() {
//        placePiece("e1", PieceType.KING, PieceColor.WHITE);
//        placePiece("f3", PieceType.KNIGHT, PieceColor.BLACK);
//
//        assertTrue(checkDetector.isInCheck(board, PieceColor.WHITE));
//    }
//
//    @Test
//    void shouldDetectCheckFromPawn() {
//        placePiece("e4", PieceType.KING, PieceColor.WHITE);
//        placePiece("d5", PieceType.PAWN, PieceColor.BLACK);
//
//        assertTrue(checkDetector.isInCheck(board, PieceColor.WHITE));
//    }
//
//    @Test
//    void shouldNotDetectCheckThroughBlockingPiece() {
//        placePiece("e1", PieceType.KING, PieceColor.WHITE);
//        placePiece("e8", PieceType.ROOK, PieceColor.BLACK);
//        placePiece("e4", PieceType.PAWN, PieceColor.WHITE);
//
//        assertFalse(checkDetector.isInCheck(board, PieceColor.WHITE));
//    }
//
//    @Test
//    void shouldDetectCheckAgainstBlackKing() {
//        placePiece("e8", PieceType.KING, PieceColor.BLACK);
//        placePiece("e1", PieceType.ROOK, PieceColor.WHITE);
//
//        assertTrue(checkDetector.isInCheck(board, PieceColor.BLACK));
//    }
//
//    @Test
//    void shouldThrowExceptionWhenKingIsMissing() {
//        assertThrows(
//                IllegalStateException.class,
//                () -> checkDetector.isInCheck(board, PieceColor.WHITE)
//        );
//    }
//
//    private void placePiece(
//            String square,
//            PieceType type,
//            PieceColor color) {
//
//        board.setPiece(
//                Position.fromAlgebraic(square),
//                new Piece(type, color)
//        );
//    }
//}
