
package com.castleops.chess.engine;

import com.castleops.chess.model.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MoveValidatorTest {

    private final MoveValidator validator = new MoveValidator();

    @Test
    void shouldRejectCapturingOwnPiece() {
        Board board = new Board();

        board.setPiece(
                Position.fromAlgebraic("e4"),
                new Piece(PieceType.PAWN, PieceColor.WHITE)
        );

        board.setPiece(
                Position.fromAlgebraic("d5"),
                new Piece(PieceType.KNIGHT, PieceColor.WHITE)
        );

        Move move = new Move(
                Position.fromAlgebraic("e4"),
                Position.fromAlgebraic("d5")
        );

        assertFalse(validator.isValidMove(board, move));
    }

//    used for when the pieces logic wasn`t ready
//    @Test
//    void shouldRejectMoveForUnsupportedPieceType() {
//        Board board = new Board();
//
//        board.setPiece(
//                Position.fromAlgebraic("a1"),
//                new Piece(PieceType.KING, PieceColor.WHITE)
//        );
//
//        Move move = new Move(
//                Position.fromAlgebraic("a1"),
//                Position.fromAlgebraic("a4")
//        );
//
//        assertFalse(validator.isValidMove(board, move));
//    }


    @Test
    void shouldDelegateValidPawnMoveToPawnValidator() {
        Board board = new Board();
        board.setupInitialPosition();

        Move move = new Move(
                Position.fromAlgebraic("e2"),
                Position.fromAlgebraic("e4")
        );

        assertTrue(validator.isValidMove(board, move));
    }


}
