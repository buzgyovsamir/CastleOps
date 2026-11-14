package com.castleops.chess.game;

import com.castleops.chess.engine.validator.LegalMoveValidator;
import com.castleops.chess.model.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HalfmoveClockTest {

    private final MoveExecutor executor = new MoveExecutor();

    @Test
    void shouldStartAtZero() {
        GameState state = new GameState();

        assertEquals(0, state.getHalfmoveClock());
    }

    @Test
    void shouldCountQuietMovesForBothPlayers() {
        GameState state = new GameState();

        executor.apply(state, move("g1", "f3"));
        state.switchTurn();

        executor.apply(state, move("g8", "f6"));

        assertEquals(2, state.getHalfmoveClock());
    }

    @Test
    void shouldResetAfterPawnMove() {
        GameState state = new GameState();

        executor.apply(state, move("g1", "f3"));
        state.switchTurn();

        executor.apply(state, move("g8", "f6"));
        state.switchTurn();

        assertEquals(2, state.getHalfmoveClock());

        executor.apply(state, move("e2", "e4"));

        assertEquals(0, state.getHalfmoveClock());
    }

    @Test
    void shouldResetAfterCaptureByNonPawnPiece() {
        GameState state = new GameState();

        executor.apply(state, move("e2", "e4"));
        state.switchTurn();

        executor.apply(state, move("d7", "d5"));
        state.switchTurn();

        executor.apply(state, move("b1", "c3"));
        state.switchTurn();

        executor.apply(state, move("g8", "f6"));
        state.switchTurn();

        assertEquals(2, state.getHalfmoveClock());

        executor.apply(state, move("c3", "d5"));

        assertEquals(0, state.getHalfmoveClock());
        assertEquals(
                new Piece(PieceType.KNIGHT, PieceColor.WHITE),
                state.getBoard().getPiece(position("d5"))
        );
    }

    @Test
    void shouldRestoreClockAfterUndoingPawnMove() {
        GameState state = new GameState();

        executor.apply(state, move("g1", "f3"));
        state.switchTurn();

        MoveExecutor.UndoState undoState =
                executor.apply(state, move("e7", "e5"));

        assertEquals(0, state.getHalfmoveClock());

        executor.undo(state, undoState);

        assertEquals(1, state.getHalfmoveClock());
        assertEquals(
                new Piece(PieceType.PAWN, PieceColor.BLACK),
                state.getBoard().getPiece(position("e7"))
        );
        assertNull(state.getBoard().getPiece(position("e5")));
    }

    @Test
    void shouldRestoreClockAfterUndoingQuietMove() {
        GameState state = new GameState();

        MoveExecutor.UndoState undoState =
                executor.apply(state, move("g1", "f3"));

        assertEquals(1, state.getHalfmoveClock());

        executor.undo(state, undoState);

        assertEquals(0, state.getHalfmoveClock());
    }

    @Test
    void shouldPreserveClockDuringLegalMoveValidation() {
        GameState state = new GameState();
        LegalMoveValidator validator = new LegalMoveValidator();

        executor.apply(state, move("g1", "f3"));
        state.switchTurn();

        assertEquals(1, state.getHalfmoveClock());

        // Пробен ход, който временно увеличава брояча.
        assertTrue(validator.isLegalMove(state, move("g8", "f6")));
        assertEquals(1, state.getHalfmoveClock());

        // Пробен ход, който временно нулира брояча.
        assertTrue(validator.isLegalMove(state, move("e7", "e5")));
        assertEquals(1, state.getHalfmoveClock());
    }

    private Position position(String square) {
        return Position.fromAlgebraic(square);
    }

    private Move move(String from, String to) {
        return new Move(position(from), position(to));
    }
}