package com.castleops.chess.engine.generator;

import com.castleops.chess.game.GameState;
import com.castleops.chess.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LegalMoveGeneratorTest {

    private GameState gameState;
    private Board board;
    private LegalMoveGenerator generator;

    @BeforeEach
    void setUp() {
        gameState = new GameState();
        board = gameState.getBoard();
        generator = new LegalMoveGenerator();
    }

    @Test
    void shouldGenerateTwentyMovesInInitialPosition() {
        List<Move> moves = generator.generateLegalMoves(gameState);

        assertEquals(20, moves.size());
        assertEquals(20L, moves.stream().distinct().count());

        assertTrue(moves.contains(move("e2", "e3")));
        assertTrue(moves.contains(move("e2", "e4")));
        assertTrue(moves.contains(move("g1", "f3")));
        assertTrue(moves.contains(move("g1", "h3")));
    }

    @Test
    void shouldGenerateMovesOnlyForCurrentPlayer() {
        gameState.switchTurn();

        List<Move> moves = generator.generateLegalMoves(gameState);

        assertEquals(20, moves.size());
        assertTrue(moves.contains(move("e7", "e5")));

        for (Move move : moves) {
            Piece piece = board.getPiece(move.from());
            assertEquals(PieceColor.BLACK, piece.color());
        }
    }

    @Test
    void shouldPreserveEntireStateAfterGeneration() {
        Piece[][] originalPieces = new Piece[8][8];

        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                originalPieces[rank][file] =
                        board.getPiece(new Position(file, rank));
            }
        }

        PieceColor originalTurn = gameState.getCurrentTurn();
        var originalStatus = gameState.getStatus();
        Position whiteKing = gameState.getKingPosition(PieceColor.WHITE);
        Position blackKing = gameState.getKingPosition(PieceColor.BLACK);

        generator.generateLegalMoves(gameState);

        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                assertEquals(
                        originalPieces[rank][file],
                        board.getPiece(new Position(file, rank))
                );
            }
        }

        assertEquals(originalTurn, gameState.getCurrentTurn());
        assertEquals(originalStatus, gameState.getStatus());
        assertEquals(whiteKing,
                gameState.getKingPosition(PieceColor.WHITE));
        assertEquals(blackKing,
                gameState.getKingPosition(PieceColor.BLACK));
    }

    @Test
    void shouldExcludeMoveThatExposesKing() {
        clearBoard();

        placePiece("e1", PieceType.KING, PieceColor.WHITE);
        placePiece("a8", PieceType.KING, PieceColor.BLACK);
        placePiece("e2", PieceType.ROOK, PieceColor.WHITE);
        placePiece("e8", PieceType.ROOK, PieceColor.BLACK);

        List<Move> moves = generator.generateLegalMoves(gameState);

        assertFalse(moves.contains(move("e2", "f2")));
        assertTrue(moves.contains(move("e2", "e3")));
        assertTrue(moves.contains(move("e2", "e8")));
    }

    @Test
    void shouldGenerateOnlyEscapeMovesWhenKingIsInCheck() {
        clearBoard();

        placePiece("e1", PieceType.KING, PieceColor.WHITE);
        placePiece("a8", PieceType.KING, PieceColor.BLACK);
        placePiece("e8", PieceType.ROOK, PieceColor.BLACK);

        List<Move> moves = generator.generateLegalMoves(gameState);

        assertEquals(4, moves.size());
        assertTrue(moves.containsAll(List.of(
                move("e1", "d1"),
                move("e1", "f1"),
                move("e1", "d2"),
                move("e1", "f2")
        )));
    }

    @Test
    void shouldReturnNoMovesInCheckmatePosition() {
        clearBoard();

        placePiece("h1", PieceType.KING, PieceColor.WHITE);
        placePiece("f3", PieceType.KING, PieceColor.BLACK);
        placePiece("g2", PieceType.QUEEN, PieceColor.BLACK);

        assertTrue(generator.generateLegalMoves(gameState).isEmpty());
    }

    @Test
    void shouldReturnNoMovesInStalematePosition() {
        clearBoard();

        placePiece("h1", PieceType.KING, PieceColor.WHITE);
        placePiece("f2", PieceType.KING, PieceColor.BLACK);
        placePiece("g3", PieceType.QUEEN, PieceColor.BLACK);

        assertTrue(generator.generateLegalMoves(gameState).isEmpty());
    }

    @Test
    void shouldGenerateAllFourPromotions() {
        clearBoard();

        placePiece("a1", PieceType.KING, PieceColor.WHITE);
        placePiece("h6", PieceType.KING, PieceColor.BLACK);
        placePiece("e7", PieceType.PAWN, PieceColor.WHITE);

        Position from = Position.fromAlgebraic("e7");
        Position to = Position.fromAlgebraic("e8");

        List<Move> pawnMoves = generator.generateLegalMoves(gameState)
                .stream()
                .filter(move -> move.from().equals(from))
                .toList();

        assertEquals(4, pawnMoves.size());

        for (PieceType type : List.of(
                PieceType.QUEEN,
                PieceType.ROOK,
                PieceType.BISHOP,
                PieceType.KNIGHT)) {

            assertTrue(pawnMoves.contains(new Move(from, to, type)));
        }

        assertEquals(new Piece(PieceType.PAWN, PieceColor.WHITE),
                board.getPiece(from));
        assertNull(board.getPiece(to));
    }

    @Test
    void shouldGenerateForwardAndCapturePromotionsForBlack() {
        clearBoard();

        placePiece("h3", PieceType.KING, PieceColor.WHITE);
        placePiece("a8", PieceType.KING, PieceColor.BLACK);
        placePiece("e2", PieceType.PAWN, PieceColor.BLACK);
        placePiece("f1", PieceType.ROOK, PieceColor.WHITE);

        gameState.switchTurn();

        Position from = Position.fromAlgebraic("e2");

        List<Move> pawnMoves = generator.generateLegalMoves(gameState)
                .stream()
                .filter(move -> move.from().equals(from))
                .toList();

        assertEquals(8, pawnMoves.size());

        for (PieceType type : List.of(
                PieceType.QUEEN,
                PieceType.ROOK,
                PieceType.BISHOP,
                PieceType.KNIGHT)) {

            assertTrue(pawnMoves.contains(new Move(
                    from, Position.fromAlgebraic("e1"), type
            )));

            assertTrue(pawnMoves.contains(new Move(
                    from, Position.fromAlgebraic("f1"), type
            )));
        }

        assertEquals(new Piece(PieceType.PAWN, PieceColor.BLACK),
                board.getPiece(from));
        assertEquals(new Piece(PieceType.ROOK, PieceColor.WHITE),
                board.getPiece(Position.fromAlgebraic("f1")));
    }

    private void clearBoard() {
        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                board.setPiece(new Position(file, rank), null);
            }
        }
    }

    private void placePiece(
            String square,
            PieceType type,
            PieceColor color) {

        Position position = Position.fromAlgebraic(square);
        board.setPiece(position, new Piece(type, color));

        if (type == PieceType.KING) {
            gameState.updateKingPosition(color, position);
        }
    }

    private Move move(String from, String to) {
        return new Move(
                Position.fromAlgebraic(from),
                Position.fromAlgebraic(to)
        );
    }
}