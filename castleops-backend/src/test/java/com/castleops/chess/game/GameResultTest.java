package com.castleops.chess.game;

import com.castleops.chess.model.PieceColor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

class GameResultTest {

    @ParameterizedTest
    @EnumSource(PieceColor.class)
    void shouldCreateCheckmateResultWithWinner(PieceColor winner) {
        GameResult result = GameResult.checkmate(winner);

        assertEquals(GameEndReason.CHECKMATE, result.reason());
        assertEquals(winner, result.winner());
        assertFalse(result.isDraw());
    }

    @ParameterizedTest
    @EnumSource(
            value = GameEndReason.class,
            names = {"CHECKMATE", "RESIGNATION"},
            mode = EnumSource.Mode.EXCLUDE
    )
    void shouldCreateDrawWithoutWinner(GameEndReason reason) {
        GameResult result = GameResult.draw(reason);

        assertEquals(reason, result.reason());
        assertNull(result.winner());
        assertTrue(result.isDraw());
    }

    @Test
    void shouldRejectCheckmateWithoutWinner() {
        assertThrows(
                NullPointerException.class,
                () -> new GameResult(GameEndReason.CHECKMATE, null)
        );
    }

    @ParameterizedTest
    @EnumSource(
            value = GameEndReason.class,
            names = {"CHECKMATE", "RESIGNATION"},
            mode = EnumSource.Mode.EXCLUDE
    )
    void shouldRejectDrawWithWinner(GameEndReason reason) {
        assertThrows(
                IllegalArgumentException.class,
                () -> new GameResult(reason, PieceColor.WHITE)
        );
    }

    @Test
    void shouldRejectCheckmateAsDrawReason() {
        assertThrows(
                IllegalArgumentException.class,
                () -> GameResult.draw(GameEndReason.CHECKMATE)
        );
    }

    @Test
    void shouldRejectMissingReason() {
        assertThrows(
                NullPointerException.class,
                () -> new GameResult(null, null)
        );
    }
}