
package com.castleops.chess.game;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GameStatusTest {

    @Test
    void shouldContainExpectedStatuses() {
        Set<GameStatus> actualStatuses =
                Set.of(GameStatus.values());

        assertEquals(
                Set.of(
                        GameStatus.ACTIVE,
                        GameStatus.CHECKMATE,
                        GameStatus.STALEMATE,
                        GameStatus.DRAW,
                        GameStatus.RESIGNED
                ),
                Set.of(GameStatus.values())
        );
    }
}
