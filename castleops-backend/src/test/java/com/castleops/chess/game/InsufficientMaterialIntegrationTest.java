package com.castleops.chess.game;

import com.castleops.chess.model.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InsufficientMaterialIntegrationTest {

    @Test
    void shouldDrawWhenCaptureLeavesOnlyKings() {
        GameState state = createKingsOnlyState();

        state.getBoard().setPiece(
                position("e2"),
                new Piece(PieceType.ROOK, PieceColor.BLACK)
        );

        ChessGame game = new ChessGame(state);

        assertEquals(GameStatus.ACTIVE, game.getStatus());

        assertTrue(game.makeMove(move("e1", "e2")));

        assertEquals(GameStatus.DRAW, game.getStatus());
        assertEquals(
                new Piece(PieceType.KING, PieceColor.WHITE),
                game.getPiece(position("e2"))
        );

        assertFalse(game.makeMove(move("e8", "e7")));
    }

    @Test
    void shouldRecognizeKingsOnlyStartingPositionAsDraw() {
        ChessGame game = new ChessGame(createKingsOnlyState());

        assertEquals(GameStatus.DRAW, game.getStatus());
        assertFalse(game.makeMove(move("e1", "e2")));
    }

    @Test
    void shouldDrawWhenCaptureLeavesOnlySameSquareColorBishops() {
        GameState state = createKingsOnlyState();

        state.getBoard().setPiece(
                position("c1"),
                new Piece(PieceType.BISHOP, PieceColor.WHITE)
        );
        state.getBoard().setPiece(
                position("f8"),
                new Piece(PieceType.BISHOP, PieceColor.BLACK)
        );
        state.getBoard().setPiece(
                position("d2"),
                new Piece(PieceType.KNIGHT, PieceColor.BLACK)
        );

        ChessGame game = new ChessGame(state);

        assertEquals(GameStatus.ACTIVE, game.getStatus());

        assertTrue(game.makeMove(move("c1", "d2")));

        assertEquals(GameStatus.DRAW, game.getStatus());
        assertEquals(
                new Piece(PieceType.BISHOP, PieceColor.WHITE),
                game.getPiece(position("d2"))
        );
        assertFalse(game.makeMove(move("e8", "e7")));
    }

    private GameState createKingsOnlyState() {
        GameState state = new GameState();

        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                state.getBoard().setPiece(
                        new Position(file, rank),
                        null
                );
            }
        }

        state.getBoard().setPiece(
                position("e1"),
                new Piece(PieceType.KING, PieceColor.WHITE)
        );
        state.getBoard().setPiece(
                position("e8"),
                new Piece(PieceType.KING, PieceColor.BLACK)
        );

        state.updateKingPosition(PieceColor.WHITE, position("e1"));
        state.updateKingPosition(PieceColor.BLACK, position("e8"));
        state.restoreCastlingRights(CastlingRights.none());

        return state;
    }

    private Move move(String from, String to) {
        return new Move(position(from), position(to));
    }

    private Position position(String square) {
        return Position.fromAlgebraic(square);
    }
}