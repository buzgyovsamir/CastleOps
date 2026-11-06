
package com.castleops.chess.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PieceTest {

    @Test
    void shouldCreateWhiteQueen() {
        Piece piece = new Piece(
                PieceType.QUEEN,
                PieceColor.WHITE
        );

        assertEquals(PieceType.QUEEN, piece.type());
        assertEquals(PieceColor.WHITE, piece.color());
    }

    @Test
    void shouldRejectNullType() {
        assertThrows(NullPointerException.class,
                () -> new Piece(null, PieceColor.WHITE));
    }

    @Test
    void shouldRejectNullColor() {
        assertThrows(NullPointerException.class,
                () -> new Piece(PieceType.KING, null));
    }

    @Test
    void shouldConsiderIdenticalPiecesEqual() {
        Piece first = new Piece(PieceType.ROOK, PieceColor.BLACK);
        Piece second = new Piece(PieceType.ROOK, PieceColor.BLACK);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void shouldReturnOppositeColor() {
        assertEquals(PieceColor.BLACK, PieceColor.WHITE.opposite());
        assertEquals(PieceColor.WHITE, PieceColor.BLACK.opposite());
    }
}
