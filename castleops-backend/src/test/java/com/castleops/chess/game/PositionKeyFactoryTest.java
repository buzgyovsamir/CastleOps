package com.castleops.chess.game;

import com.castleops.chess.engine.PositionKeyFactory;
import com.castleops.chess.model.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PositionKeyFactoryTest {

    private final PositionKeyFactory factory = new PositionKeyFactory();

    @Test
    void shouldCreateEqualKeysForEqualPositions() {
        assertEquals(
                factory.create(new GameState()),
                factory.create(new GameState())
        );
    }

    @Test
    void shouldDistinguishPlayerToMove() {
        GameState whiteTurn = new GameState();
        GameState blackTurn = new GameState();
        blackTurn.switchTurn();

        assertNotEquals(
                factory.create(whiteTurn),
                factory.create(blackTurn)
        );
    }

    @Test
    void shouldDistinguishCastlingRights() {
        GameState withRights = new GameState();
        GameState withoutWhiteKingside = new GameState();

        withoutWhiteKingside.revokeCastlingRight(
                PieceColor.WHITE,
                CastlingSide.KING_SIDE
        );

        assertNotEquals(
                factory.create(withRights),
                factory.create(withoutWhiteKingside)
        );
    }

    @Test
    void shouldIgnoreHalfmoveClock() {
        GameState first = new GameState();
        GameState second = new GameState();
        second.restoreHalfmoveClock(80);

        assertEquals(factory.create(first), factory.create(second));
    }

    @Test
    void shouldKeepPreviouslyCreatedKeyUnchanged() {
        GameState state = new GameState();
        PositionKey originalKey = factory.create(state);

        state.getBoard().setPiece(position("a2"), null);

        assertEquals(factory.create(new GameState()), originalKey);
        assertNotEquals(originalKey, factory.create(state));
    }

    @Test
    void shouldIgnoreEnPassantTargetWithoutCapturingPawn() {
        GameState state = new GameState();
        MoveExecutor executor = new MoveExecutor();

        executor.apply(state, move("e2", "e4"));
        state.switchTurn();

        PositionKey withTarget = factory.create(state);

        assertEquals(position("e3"), state.getEnPassantTarget());
        assertNull(withTarget.legalEnPassantTarget());

        state.updateEnPassantTarget(null);

        assertEquals(withTarget, factory.create(state));
    }

    @Test
    void shouldIncludeLegalEnPassantTargetAndPreserveState() {
        GameState state = emptyState();

        placePiece(state, "a1", PieceType.KING, PieceColor.WHITE);
        placePiece(state, "h8", PieceType.KING, PieceColor.BLACK);
        placePiece(state, "e5", PieceType.PAWN, PieceColor.WHITE);
        placePiece(state, "d5", PieceType.PAWN, PieceColor.BLACK);
        state.updateEnPassantTarget(position("d6"));
        state.restoreHalfmoveClock(0);

        PositionKey withTarget = factory.create(state);

        assertEquals(position("d6"), withTarget.legalEnPassantTarget());

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
        assertEquals(0, state.getHalfmoveClock());
        assertEquals(CastlingRights.none(), state.getCastlingRights());

        state.updateEnPassantTarget(null);

        assertNotEquals(withTarget, factory.create(state));
    }

    @Test
    void shouldIgnoreEnPassantThatWouldExposeOwnKing() {
        GameState state = emptyState();

        placePiece(state, "h5", PieceType.KING, PieceColor.WHITE);
        placePiece(state, "h8", PieceType.KING, PieceColor.BLACK);
        placePiece(state, "g5", PieceType.PAWN, PieceColor.WHITE);
        placePiece(state, "f5", PieceType.PAWN, PieceColor.BLACK);
        placePiece(state, "a5", PieceType.ROOK, PieceColor.BLACK);
        state.updateEnPassantTarget(position("f6"));

        PositionKey withTarget = factory.create(state);

        assertNull(withTarget.legalEnPassantTarget());
        assertEquals(position("f6"), state.getEnPassantTarget());

        state.updateEnPassantTarget(null);

        assertEquals(withTarget, factory.create(state));
    }

    @Test
    void shouldIncludeLegalEnPassantForBlack() {
        GameState state = emptyState();

        placePiece(state, "a1", PieceType.KING, PieceColor.WHITE);
        placePiece(state, "h8", PieceType.KING, PieceColor.BLACK);
        placePiece(state, "d4", PieceType.PAWN, PieceColor.BLACK);
        placePiece(state, "e4", PieceType.PAWN, PieceColor.WHITE);

        state.switchTurn();
        state.updateEnPassantTarget(position("e3"));

        assertEquals(
                position("e3"),
                factory.create(state).legalEnPassantTarget()
        );
        assertEquals(PieceColor.BLACK, state.getCurrentTurn());
    }

    private GameState emptyState() {
        GameState state = new GameState();

        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                state.getBoard().setPiece(new Position(file, rank), null);
            }
        }

        state.revokeCastlingRights(PieceColor.WHITE);
        state.revokeCastlingRights(PieceColor.BLACK);

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