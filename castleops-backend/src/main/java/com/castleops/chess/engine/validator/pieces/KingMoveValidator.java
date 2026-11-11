
package com.castleops.chess.engine.validator.pieces;

import com.castleops.chess.engine.validator.DestinationValidator;
import com.castleops.chess.model.*;

import java.util.Objects;

public class KingMoveValidator implements PieceMoveValidator {

    private final DestinationValidator destinationValidator =
            new DestinationValidator();

    @Override
    public boolean isValidMove(Board board, Move move) {
        Objects.requireNonNull(board, "Board cannot be null");
        Objects.requireNonNull(move, "Move cannot be null");

        Piece piece = board.getPiece(move.from());

        if (piece == null || piece.type() != PieceType.KING) {
            return false;
        }

        return isSingleStepMove(move)
                && destinationValidator.isDestinationAvailable(board, move, piece);
    }

    private boolean isSingleStepMove(Move move) {
        int fileDifference = Math.abs(
                move.to().file() - move.from().file()
        );

        int rankDifference = Math.abs(
                move.to().rank() - move.from().rank()
        );

        return fileDifference <= 1 && rankDifference <= 1;
    }
//    exported logic in the MoveValidator class
//    private boolean isDestinationAvailable(Board board, Move move, Piece piece) {
//        Piece targetPiece = board.getPiece(move.to());
//
//        return targetPiece == null
//                || targetPiece.color() != piece.color();
//    }
}
