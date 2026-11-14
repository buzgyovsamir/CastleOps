package com.castleops.chess.game;

import com.castleops.chess.engine.GameStatusEvaluator;
import com.castleops.chess.engine.MoveCountDrawRule;
import com.castleops.chess.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class MoveCountDrawRuleTest {

    private final MoveCountDrawRule rule = new MoveCountDrawRule();
    private final GameStatusEvaluator evaluator = new GameStatusEvaluator();

    @ParameterizedTest
    @CsvSource({
            "0, false, false",
            "99, false, false",
            "100, true, false",
            "149, true, false",
            "150, true, true",
            "151, true, true"
    })
    void shouldRecognizeThresholds(
            int halfmoveClock,
            boolean expectedClaimThreshold,
            boolean expectedAutomaticThreshold) {

        GameState state = new GameState();
        state.restoreHalfmoveClock(halfmoveClock);

        assertEquals(
                expectedClaimThreshold,
                rule.hasReachedClaimThreshold(state)
        );

        assertEquals(
                expectedAutomaticThreshold,
                rule.hasReachedAutomaticDrawThreshold(state)
        );
    }

    @Test
    void shouldNotAutomaticallyDrawAtFiftyMoves() {
        GameState state = activePosition();
        state.restoreHalfmoveClock(100);

        assertEquals(GameStatus.ACTIVE, evaluator.evaluate(state));
    }

    @Test
    void shouldAutomaticallyDrawAtSeventyFiveMoves() {
        GameState state = activePosition();
        state.restoreHalfmoveClock(150);

        assertEquals(GameStatus.DRAW, evaluator.evaluate(state));

        // Evaluator-ът връща резултат, без сам да променя състоянието.
        assertEquals(GameStatus.ACTIVE, state.getStatus());
        assertEquals(150, state.getHalfmoveClock());
    }

    @Test
    void shouldPreferCheckmateOverAutomaticDraw() {
        GameState state = emptyPosition();

        placePiece(state, "h1", PieceType.KING, PieceColor.WHITE);
        placePiece(state, "f3", PieceType.KING, PieceColor.BLACK);
        placePiece(state, "g2", PieceType.QUEEN, PieceColor.BLACK);

        state.restoreHalfmoveClock(150);

        assertEquals(GameStatus.CHECKMATE, evaluator.evaluate(state));
    }

    @Test
    void shouldKeepStalemateStatusAtAutomaticDrawThreshold() {
        GameState state = emptyPosition();

        placePiece(state, "h1", PieceType.KING, PieceColor.WHITE);
        placePiece(state, "f2", PieceType.KING, PieceColor.BLACK);
        placePiece(state, "g3", PieceType.QUEEN, PieceColor.BLACK);

        state.restoreHalfmoveClock(150);

        assertEquals(GameStatus.STALEMATE, evaluator.evaluate(state));
    }

    @Test
    void shouldAutomaticallyDrawWhenCheckedKingCanEscape() {
        GameState state = emptyPosition();

        placePiece(state, "e1", PieceType.KING, PieceColor.WHITE);
        placePiece(state, "a8", PieceType.KING, PieceColor.BLACK);
        placePiece(state, "e8", PieceType.ROOK, PieceColor.BLACK);

        state.restoreHalfmoveClock(150);

        assertEquals(GameStatus.DRAW, evaluator.evaluate(state));
    }

    private GameState activePosition() {
        GameState state = emptyPosition();

        placePiece(state, "e1", PieceType.KING, PieceColor.WHITE);
        placePiece(state, "e8", PieceType.KING, PieceColor.BLACK);
        placePiece(state, "a2", PieceType.ROOK, PieceColor.WHITE);

        return state;
    }

    private GameState emptyPosition() {
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

        Position position = Position.fromAlgebraic(square);
        state.getBoard().setPiece(position, new Piece(type, color));

        if (type == PieceType.KING) {
            state.updateKingPosition(color, position);
        }
    }
}