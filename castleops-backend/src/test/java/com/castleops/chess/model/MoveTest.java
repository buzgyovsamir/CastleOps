
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

    @Test
    void shouldCreateRegularMoveWithoutPromotion() {
        Move move = new Move(
                Position.fromAlgebraic("e2"),
                Position.fromAlgebraic("e4")
        );

        assertFalse(move.isPromotion());
        assertNull(move.promotionType());
    }

    @Test
    void shouldAllowAllFourPromotionTypes() {
        PieceType[] allowedTypes = {
                PieceType.QUEEN,
                PieceType.ROOK,
                PieceType.BISHOP,
                PieceType.KNIGHT
        };

        for (PieceType type : allowedTypes) {
            Move move = new Move(
                    Position.fromAlgebraic("e7"),
                    Position.fromAlgebraic("e8"),
                    type
            );

            assertTrue(move.isPromotion());
            assertEquals(type, move.promotionType());
        }
    }

    @Test
    void shouldRejectPromotionToKing() {
        assertThrows(IllegalArgumentException.class, () -> new Move(
                Position.fromAlgebraic("e7"),
                Position.fromAlgebraic("e8"),
                PieceType.KING
        ));
    }

    @Test
    void shouldRejectPromotionToPawn() {
        assertThrows(IllegalArgumentException.class, () -> new Move(
                Position.fromAlgebraic("e7"),
                Position.fromAlgebraic("e8"),
                PieceType.PAWN
        ));
    }

    @Test
    void shouldDistinguishMovesWithDifferentPromotionTypes() {
        Position from = Position.fromAlgebraic("e7");
        Position to = Position.fromAlgebraic("e8");

        Move queenPromotion = new Move(from, to, PieceType.QUEEN);
        Move knightPromotion = new Move(from, to, PieceType.KNIGHT);

        assertNotEquals(queenPromotion, knightPromotion);
    }
}
