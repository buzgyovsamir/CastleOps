
package com.castleops.chess.game;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GameStatusTest {

    @Test
    void shouldContainExpectedStatuses() {
        Set<GameStatus> expectedStatuses = Set.of(
                GameStatus.ACTIVE,
                GameStatus.CHECKMATE,
                GameStatus.STALEMATE,
                GameStatus.DRAW
        );

        Set<GameStatus> actualStatuses =
                Set.of(GameStatus.values());

        assertEquals(expectedStatuses, actualStatuses);
    }
}
