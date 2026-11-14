package com.castleops.chess.engine;

import com.castleops.chess.engine.generator.LegalMoveGenerator;
import com.castleops.chess.engine.validator.LegalMoveValidator;
import com.castleops.chess.game.ChessGame;
import com.castleops.chess.game.GameState;
import com.castleops.chess.model.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnPassantIntegrationTest {

    @Test
    void shouldExecuteWhiteEnPassant() {
        ChessGame game = gameWithWhiteEnPassantAvailable();

        assertTrue(game.makeMove(move("e5", "d6")));

        assertNull(game.getPiece(position("e5")));
        assertNull(game.getPiece(position("d5")));
        assertEquals(
                new Piece(PieceType.PAWN, PieceColor.WHITE),
                game.getPiece(position("d6"))
        );
        assertEquals(PieceColor.BLACK, game.getCurrentTurn());
    }

    @Test
    void shouldExecuteBlackEnPassant() {
        ChessGame game = new ChessGame();

        playMoves(
                game,
                move("a2", "a3"),
                move("e7", "e5"),
                move("a3", "a4"),
                move("e5", "e4"),
                move("d2", "d4")
        );

        assertTrue(game.makeMove(move("e4", "d3")));

        assertNull(game.getPiece(position("e4")));
        assertNull(game.getPiece(position("d4")));
        assertEquals(
                new Piece(PieceType.PAWN, PieceColor.BLACK),
                game.getPiece(position("d3"))
        );
        assertEquals(PieceColor.WHITE, game.getCurrentTurn());
    }

    @Test
    void shouldExpireEnPassantAfterAnotherMove() {
        ChessGame game = gameWithWhiteEnPassantAvailable();

        playMoves(
                game,
                move("h2", "h3"),
                move("a6", "a5")
        );

        assertFalse(game.makeMove(move("e5", "d6")));

        assertEquals(
                new Piece(PieceType.PAWN, PieceColor.WHITE),
                game.getPiece(position("e5"))
        );
        assertEquals(
                new Piece(PieceType.PAWN, PieceColor.BLACK),
                game.getPiece(position("d5"))
        );
        assertNull(game.getPiece(position("d6")));
        assertEquals(PieceColor.WHITE, game.getCurrentTurn());
    }

    @Test
    void shouldPreserveEnPassantAfterRejectedMove() {
        ChessGame game = gameWithWhiteEnPassantAvailable();

        assertFalse(game.makeMove(move("e5", "e7")));
        assertEquals(PieceColor.WHITE, game.getCurrentTurn());

        assertTrue(game.makeMove(move("e5", "d6")));
        assertNull(game.getPiece(position("d5")));
        assertEquals(
                new Piece(PieceType.PAWN, PieceColor.WHITE),
                game.getPiece(position("d6"))
        );
    }

    @Test
    void shouldRejectEnPassantThatExposesKingAndRestoreBoard() {
        GameState state = emptyState();
        Board board = state.getBoard();

        placePiece(state, "h5", PieceType.KING, PieceColor.WHITE);
        placePiece(state, "h8", PieceType.KING, PieceColor.BLACK);
        placePiece(state, "g5", PieceType.PAWN, PieceColor.WHITE);
        placePiece(state, "f5", PieceType.PAWN, PieceColor.BLACK);
        placePiece(state, "a5", PieceType.ROOK, PieceColor.BLACK);
        state.updateEnPassantTarget(position("f6"));

        LegalMoveValidator validator = new LegalMoveValidator();

        assertFalse(validator.isLegalMove(state, move("g5", "f6")));

        assertEquals(
                new Piece(PieceType.PAWN, PieceColor.WHITE),
                board.getPiece(position("g5"))
        );
        assertEquals(
                new Piece(PieceType.PAWN, PieceColor.BLACK),
                board.getPiece(position("f5"))
        );
        assertNull(board.getPiece(position("f6")));
        assertEquals(position("h5"),
                state.getKingPosition(PieceColor.WHITE));
        assertEquals(position("f6"), state.getEnPassantTarget());
        assertEquals(PieceColor.WHITE, state.getCurrentTurn());
    }

    @Test
    void shouldAllowEnPassantToRemoveCheckAndRestoreBoard() {
        GameState state = emptyState();
        Board board = state.getBoard();

        placePiece(state, "e4", PieceType.KING, PieceColor.WHITE);
        placePiece(state, "a8", PieceType.KING, PieceColor.BLACK);
        placePiece(state, "e5", PieceType.PAWN, PieceColor.WHITE);
        placePiece(state, "d5", PieceType.PAWN, PieceColor.BLACK);
        state.updateEnPassantTarget(position("d6"));

        CheckDetector checkDetector = new CheckDetector();
        LegalMoveValidator validator = new LegalMoveValidator();

        assertTrue(checkDetector.isInCheck(state, PieceColor.WHITE));
        assertTrue(validator.isLegalMove(state, move("e5", "d6")));

        assertEquals(
                new Piece(PieceType.PAWN, PieceColor.WHITE),
                board.getPiece(position("e5"))
        );
        assertEquals(
                new Piece(PieceType.PAWN, PieceColor.BLACK),
                board.getPiece(position("d5"))
        );
        assertNull(board.getPiece(position("d6")));
        assertEquals(position("d6"), state.getEnPassantTarget());
        assertEquals(PieceColor.WHITE, state.getCurrentTurn());

        assertTrue(checkDetector.isInCheck(state, PieceColor.WHITE));
    }

    @Test
    void shouldGenerateEnPassantWithoutConsumingTheRight() {
        GameState state = emptyState();

        placePiece(state, "a1", PieceType.KING, PieceColor.WHITE);
        placePiece(state, "h8", PieceType.KING, PieceColor.BLACK);
        placePiece(state, "e5", PieceType.PAWN, PieceColor.WHITE);
        placePiece(state, "d5", PieceType.PAWN, PieceColor.BLACK);
        state.updateEnPassantTarget(position("d6"));

        LegalMoveGenerator generator = new LegalMoveGenerator();
        Move enPassant = move("e5", "d6");

        assertTrue(generator.generateLegalMoves(state).contains(enPassant));
        assertTrue(generator.generateLegalMoves(state).contains(enPassant));

        assertEquals(
                new Piece(PieceType.PAWN, PieceColor.WHITE),
                state.getBoard().getPiece(position("e5"))
        );
        assertEquals(
                new Piece(PieceType.PAWN, PieceColor.BLACK),
                state.getBoard().getPiece(position("d5"))
        );
        assertNull(state.getBoard().getPiece(position("d6")));
        assertEquals(position("d6"), state.getEnPassantTarget());
        assertEquals(PieceColor.WHITE, state.getCurrentTurn());
    }

    private ChessGame gameWithWhiteEnPassantAvailable() {
        ChessGame game = new ChessGame();

        playMoves(
                game,
                move("e2", "e4"),
                move("a7", "a6"),
                move("e4", "e5"),
                move("d7", "d5")
        );

        return game;
    }

    private void playMoves(ChessGame game, Move... moves) {
        for (Move move : moves) {
            assertTrue(
                    game.makeMove(move),
                    "Setup move must be legal: " + move
            );
        }
    }

    private GameState emptyState() {
        GameState state = new GameState();

        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                state.getBoard().setPiece(new Position(file, rank), null);
            }
        }

        return state;
    }

    private void placePiece(
            GameState state,
            String square,
            PieceType type,
            PieceColor color) {

        Position position = position(square);
        state.getBoard().setPiece(position, new Piece(type, color));

        if (type == PieceType.KING) {
            state.updateKingPosition(color, position);
        }
    }

    private Position position(String square) {
        return Position.fromAlgebraic(square);
    }

    private Move move(String from, String to) {
        return new Move(position(from), position(to));
    }
}