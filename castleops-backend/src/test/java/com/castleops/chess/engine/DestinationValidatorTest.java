
package com.castleops.chess.engine;

import com.castleops.chess.model.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DestinationValidatorTest {

    private final DestinationValidator validator = new DestinationValidator();

    @Test
    void shouldAllowMovingToEmptySquare() {
        Board board = new Board();
        Piece rook = new Piece(PieceType.ROOK, PieceColor.WHITE);

        Move move = new Move(
                Position.fromAlgebraic("a1"),
                Position.fromAlgebraic("a4")
        );

        assertTrue(validator.isDestinationAvailable(board, move, rook));
    }

    @Test
    void shouldAllowCapturingOpponentPiece() {
        Board board = new Board();
        Piece rook = new Piece(PieceType.ROOK, PieceColor.WHITE);

        board.setPiece(
                Position.fromAlgebraic("a4"),
                new Piece(PieceType.PAWN, PieceColor.BLACK)
        );

        Move move = new Move(
                Position.fromAlgebraic("a1"),
                Position.fromAlgebraic("a4")
        );

        assertTrue(validator.isDestinationAvailable(board, move, rook));
    }

    @Test
    void shouldRejectCapturingOwnPiece() {
        Board board = new Board();
        Piece rook = new Piece(PieceType.ROOK, PieceColor.WHITE);

        board.setPiece(
                Position.fromAlgebraic("a4"),
                new Piece(PieceType.PAWN, PieceColor.WHITE)
        );

        Move move = new Move(
                Position.fromAlgebraic("a1"),
                Position.fromAlgebraic("a4")
        );

        assertFalse(validator.isDestinationAvailable(board, move, rook));
    }
}
