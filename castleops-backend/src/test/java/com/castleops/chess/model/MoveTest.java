
package com.castleops.chess.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MoveTest {

    @Test
    void shouldCreateValidMove() {
        Position from = Position.fromAlgebraic("e2");
        Position to = Position.fromAlgebraic("e4");

        Move move = new Move(from, to);

        assertEquals(from, move.from());
        assertEquals(to, move.to());
    }

    @Test
    void shouldRejectSameSourceAndDestination() {
        Position position = Position.fromAlgebraic("e2");

        assertThrows(IllegalArgumentException.class,
                () -> new Move(position, position));
    }

    @Test
    void shouldRejectNullPositions() {
        Position e4 = Position.fromAlgebraic("e4");

        assertThrows(NullPointerException.class,
                () -> new Move(null, e4));

        assertThrows(NullPointerException.class,
                () -> new Move(e4, null));
    }

    @Test
    void shouldConsiderIdenticalMovesEqual() {
        Move first = new Move(
                Position.fromAlgebraic("e2"),
                Position.fromAlgebraic("e4")
        );

        Move second = new Move(
                Position.fromAlgebraic("e2"),
                Position.fromAlgebraic("e4")
        );

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }
}
