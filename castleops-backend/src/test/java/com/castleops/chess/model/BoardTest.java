
package com.castleops.chess.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    @Test
    void shouldCreateEmptyBoard() {
        Board board = new Board();

        assertTrue(board.isEmpty(Position.fromAlgebraic("e4")));
        assertTrue(board.isEmpty(Position.fromAlgebraic("a1")));
        assertTrue(board.isEmpty(Position.fromAlgebraic("h8")));
    }

    @Test
    void shouldPlacePieceOnBoard() {
        Board board = new Board();

        Position position = Position.fromAlgebraic("e4");
        Piece pawn = new Piece(PieceType.PAWN, PieceColor.WHITE);

        board.setPiece(position, pawn);

        assertEquals(pawn, board.getPiece(position));
        assertFalse(board.isEmpty(position));
    }

    @Test
    void shouldRemovePieceFromBoard() {
        Board board = new Board();

        Position position = Position.fromAlgebraic("e4");
        Piece pawn = new Piece(PieceType.PAWN, PieceColor.WHITE);

        board.setPiece(position, pawn);
        board.setPiece(position, null);

        assertTrue(board.isEmpty(position));
    }

    @Test
    void shouldKeepOtherSquaresUnchanged() {
        Board board = new Board();

        board.setPiece(
                Position.fromAlgebraic("e4"),
                new Piece(PieceType.QUEEN, PieceColor.WHITE)
        );

        assertTrue(board.isEmpty(Position.fromAlgebraic("e5")));
    }

    @Test
    void shouldRejectNullPosition() {
        Board board = new Board();

        assertThrows(NullPointerException.class,
                () -> board.getPiece(null));

        assertThrows(NullPointerException.class,
                () -> board.setPiece(null, null));
    }


    @Test
    void shouldSetupInitialPosition() {
        Board board = new Board();
        board.setupInitialPosition();

        assertEquals(
                new Piece(PieceType.KING, PieceColor.WHITE),
                board.getPiece(Position.fromAlgebraic("e1"))
        );

        assertEquals(
                new Piece(PieceType.QUEEN, PieceColor.BLACK),
                board.getPiece(Position.fromAlgebraic("d8"))
        );

        assertEquals(
                new Piece(PieceType.PAWN, PieceColor.WHITE),
                board.getPiece(Position.fromAlgebraic("e2"))
        );

        assertEquals(
                new Piece(PieceType.PAWN, PieceColor.BLACK),
                board.getPiece(Position.fromAlgebraic("e7"))
        );
    }

    @Test
    void shouldKeepMiddleSquaresEmpty() {
        Board board = new Board();
        board.setupInitialPosition();

        for (int rank = 2; rank <= 5; rank++) {
            for (int file = 0; file < 8; file++) {
                assertTrue(board.isEmpty(new Position(file, rank)));
            }
        }
    }

    @Test
    void shouldResetBoardToInitialPosition() {
        Board board = new Board();

        board.setPiece(
                Position.fromAlgebraic("e4"),
                new Piece(PieceType.QUEEN, PieceColor.WHITE)
        );

        board.setupInitialPosition();

        assertTrue(board.isEmpty(Position.fromAlgebraic("e4")));

        assertEquals(
                new Piece(PieceType.KING, PieceColor.WHITE),
                board.getPiece(Position.fromAlgebraic("e1"))
        );
    }

}
