package com.castleops.chess.game;

import com.castleops.chess.model.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MoveExecutorTest {

    private final MoveExecutor executor = new MoveExecutor();

    @Test
    void shouldApplyAndUndoDoublePawnMove() {
        GameState state = new GameState();

        MoveExecutor.UndoState undoState =
                executor.apply(state, move("e2", "e4"));

        assertNull(state.getBoard().getPiece(position("e2")));
        assertEquals(
                new Piece(PieceType.PAWN, PieceColor.WHITE),
                state.getBoard().getPiece(position("e4"))
        );
        assertEquals(position("e3"), state.getEnPassantTarget());
        assertEquals(PieceColor.WHITE, state.getCurrentTurn());

        executor.undo(state, undoState);

        assertEquals(
                new Piece(PieceType.PAWN, PieceColor.WHITE),
                state.getBoard().getPiece(position("e2"))
        );
        assertNull(state.getBoard().getPiece(position("e4")));
        assertNull(state.getEnPassantTarget());
        assertEquals(CastlingRights.initial(), state.getCastlingRights());
    }

    @Test
    void shouldRevokeAndRestoreKingCastlingRights() {
        GameState state = new GameState();
        state.getBoard().setPiece(position("e2"), null);

        MoveExecutor.UndoState undoState =
                executor.apply(state, move("e1", "e2"));

        assertEquals(
                new CastlingRights(false, false, true, true),
                state.getCastlingRights()
        );
        assertEquals(position("e2"),
                state.getKingPosition(PieceColor.WHITE));

        executor.undo(state, undoState);

        assertEquals(CastlingRights.initial(), state.getCastlingRights());
        assertEquals(position("e1"),
                state.getKingPosition(PieceColor.WHITE));
        assertEquals(
                new Piece(PieceType.KING, PieceColor.WHITE),
                state.getBoard().getPiece(position("e1"))
        );
        assertNull(state.getBoard().getPiece(position("e2")));
    }

    @Test
    void shouldNotRestoreRightWhenRookReturnsHome() {
        GameState state = new GameState();
        state.getBoard().setPiece(position("h2"), null);

        executor.apply(state, move("h1", "h2"));
        executor.apply(state, move("h2", "h1"));

        assertEquals(
                new CastlingRights(false, true, true, true),
                state.getCastlingRights()
        );
    }

    @Test
    void shouldRevokeCapturedRookRightAndRestoreItOnUndo() {
        GameState state = new GameState();
        Board board = state.getBoard();

        board.setPiece(position("b7"), null);
        board.setPiece(
                position("b7"),
                new Piece(PieceType.BISHOP, PieceColor.WHITE)
        );

        MoveExecutor.UndoState undoState =
                executor.apply(state, move("b7", "a8"));

        assertEquals(
                new CastlingRights(true, true, true, false),
                state.getCastlingRights()
        );

        executor.undo(state, undoState);

        assertEquals(CastlingRights.initial(), state.getCastlingRights());
        assertEquals(
                new Piece(PieceType.ROOK, PieceColor.BLACK),
                board.getPiece(position("a8"))
        );
        assertEquals(
                new Piece(PieceType.BISHOP, PieceColor.WHITE),
                board.getPiece(position("b7"))
        );
    }

    @Test
    void shouldRestorePreviousEnPassantTargetAfterUndo() {
        GameState state = new GameState();

        executor.apply(state, move("e2", "e4"));
        state.switchTurn();

        MoveExecutor.UndoState undoState =
                executor.apply(state, move("g8", "f6"));

        assertNull(state.getEnPassantTarget());

        executor.undo(state, undoState);

        assertEquals(position("e3"), state.getEnPassantTarget());
        assertEquals(PieceColor.BLACK, state.getCurrentTurn());
        assertEquals(
                new Piece(PieceType.KNIGHT, PieceColor.BLACK),
                state.getBoard().getPiece(position("g8"))
        );
        assertNull(state.getBoard().getPiece(position("f6")));
    }

    private Position position(String square) {
        return Position.fromAlgebraic(square);
    }

    private Move move(String from, String to) {
        return new Move(position(from), position(to));
    }
}