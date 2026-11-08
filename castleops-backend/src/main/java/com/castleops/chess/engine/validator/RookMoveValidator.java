
package com.castleops.chess.engine.validator;

import com.castleops.chess.engine.DestinationValidator;
import com.castleops.chess.engine.PathValidator;
import com.castleops.chess.model.*;

import java.util.Objects;

public class RookMoveValidator implements PieceMoveValidator {

    private final PathValidator pathValidator = new PathValidator();
    private final DestinationValidator destinationValidator =
            new DestinationValidator();

    @Override
    public boolean isValidMove(Board board, Move move) {
        Objects.requireNonNull(board, "Board cannot be null");
        Objects.requireNonNull(move, "Move cannot be null");

        Piece piece = board.getPiece(move.from());

        if (piece == null || piece.type() != PieceType.ROOK) {
            return false;
        }

        return isStraightMove(move)
                && pathValidator.isPathClear(board, move)
                && destinationValidator.isDestinationAvailable(board, move, piece);
    }

    private boolean isStraightMove(Move move) {
        boolean sameFile = move.from().file() == move.to().file();
        boolean sameRank = move.from().rank() == move.to().rank();

        return sameFile || sameRank;
    }
//    exported logic in the MoveValidator class
//    private boolean isDestinationAvailable(Board board, Move move, Piece piece) {
//        Piece targetPiece = board.getPiece(move.to());
//
//        return targetPiece == null
//                || targetPiece.color() != piece.color();
//    }
}
