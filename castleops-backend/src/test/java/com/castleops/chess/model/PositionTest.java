package com.castleops.chess.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PositionTest {

    @Test
    void shouldCreateValidPosition() {
        Position position = new Position(4, 1);

        assertEquals(4, position.file());
        assertEquals(1, position.rank());
    }

    @Test
    void shouldConvertAlgebraicNotationToPosition() {
        assertEquals(new Position(0, 0),
                Position.fromAlgebraic("a1"));

        assertEquals(new Position(4, 1),
                Position.fromAlgebraic("e2"));

        assertEquals(new Position(7, 7),
                Position.fromAlgebraic("h8"));
    }

    @Test
    void shouldConvertPositionToAlgebraicNotation() {
        Position position = new Position(4, 1);

        assertEquals("e2", position.toAlgebraic());
    }

    @Test
    void shouldRejectInvalidCoordinates() {
        assertThrows(IllegalArgumentException.class,
                () -> new Position(-1, 0));

        assertThrows(IllegalArgumentException.class,
                () -> new Position(8, 3));

        assertThrows(IllegalArgumentException.class,
                () -> new Position(4, 9));
    }

    @Test
    void shouldRejectInvalidAlgebraicNotation() {
        assertThrows(IllegalArgumentException.class,
                () -> Position.fromAlgebraic("z9"));

        assertThrows(IllegalArgumentException.class,
                () -> Position.fromAlgebraic("e0"));

        assertThrows(IllegalArgumentException.class,
                () -> Position.fromAlgebraic("E2"));

        assertThrows(IllegalArgumentException.class,
                () -> Position.fromAlgebraic(null));
    }

    @Test
    void shouldConsiderSameCoordinatesEqual() {
        Position first = new Position(4, 1);
        Position second = new Position(4, 1);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }
}
