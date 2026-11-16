package com.castleops.chess.game;

import com.castleops.chess.model.PieceColor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

class GameStateResultTest {

    @Test
    void shouldStartActiveWithoutResult() {
        GameState state = new GameState();

        assertEquals(GameStatus.ACTIVE, state.getStatus());
        assertTrue(state.getResult().isEmpty());
    }

    @Test
    void shouldDeriveCheckmateStatusFromResult() {
        GameState state = new GameState();
        GameResult result = GameResult.checkmate(PieceColor.BLACK);

        state.finish(result);

        assertEquals(GameStatus.CHECKMATE, state.getStatus());
        assertEquals(result, state.getResult().orElseThrow());
    }

    @ParameterizedTest
    @EnumSource(
            value = GameEndReason.class,
            names = "CHECKMATE",
            mode = EnumSource.Mode.EXCLUDE
    )
    void shouldDeriveDrawStatusFromReason(GameEndReason reason) {
        GameState state = new GameState();
        GameResult result = GameResult.draw(reason);

        state.finish(result);

        GameStatus expectedStatus = reason == GameEndReason.STALEMATE
                ? GameStatus.STALEMATE
                : GameStatus.DRAW;

        assertEquals(expectedStatus, state.getStatus());
        assertEquals(result, state.getResult().orElseThrow());
    }

    @Test
    void shouldRejectReplacingAnExistingResult() {
        GameState state = new GameState();
        GameResult originalResult =
                GameResult.checkmate(PieceColor.WHITE);

        state.finish(originalResult);

        assertThrows(
                IllegalStateException.class,
                () -> state.finish(
                        GameResult.draw(GameEndReason.FIVEFOLD_REPETITION)
                )
        );

        assertEquals(originalResult, state.getResult().orElseThrow());
        assertEquals(GameStatus.CHECKMATE, state.getStatus());
    }

    @Test
    void shouldRejectNullResultWithoutFinishingGame() {
        GameState state = new GameState();

        assertThrows(
                NullPointerException.class,
                () -> state.finish(null)
        );

        assertEquals(GameStatus.ACTIVE, state.getStatus());
        assertTrue(state.getResult().isEmpty());
    }
}