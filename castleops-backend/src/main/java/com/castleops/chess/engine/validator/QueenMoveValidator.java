
package com.castleops.chess.engine.validator;

import com.castleops.chess.engine.DestinationValidator;
import com.castleops.chess.engine.PathValidator;
import com.castleops.chess.model.*;

import java.util.Objects;

public class QueenMoveValidator implements PieceMoveValidator {

    private final PathValidator pathValidator = new PathValidator();
    private final DestinationValidator destinationValidator =
            new DestinationValidator();

    @Override
    public boolean isValidMove(Board board, Move move) {
        Objects.requireNonNull(board, "Board cannot be null");
        Objects.requireNonNull(move, "Move cannot be null");

        Piece piece = board.getPiece(move.from());

        if (piece == null || piece.type() != PieceType.QUEEN) {
            return false;
        }

        return isStraightOrDiagonalMove(move)
                && pathValidator.isPathClear(board, move)
                && destinationValidator.isDestinationAvailable(board, move, piece);
    }

    private boolean isStraightOrDiagonalMove(Move move) {
        int fileDifference = Math.abs(
                move.to().file() - move.from().file()
        );

        int rankDifference = Math.abs(
                move.to().rank() - move.from().rank()
        );

        boolean isStraight = fileDifference == 0 || rankDifference == 0;
        boolean isDiagonal = fileDifference == rankDifference;

        return isStraight || isDiagonal;
    }
//    exported logic in the MoveValidator class
//    private boolean isDestinationAvailable(Board board, Move move, Piece piece) {
//        Piece targetPiece = board.getPiece(move.to());
//
//        return targetPiece == null
//                || targetPiece.color() != piece.color();
//    }
}
