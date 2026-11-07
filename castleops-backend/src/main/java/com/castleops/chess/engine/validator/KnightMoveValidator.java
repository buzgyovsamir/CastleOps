
package com.castleops.chess.engine.validator;

import com.castleops.chess.model.*;

import java.util.Objects;

public class KnightMoveValidator implements PieceMoveValidator {

    @Override
    public boolean isValidMove(Board board, Move move) {
        Objects.requireNonNull(board, "Board cannot be null");
        Objects.requireNonNull(move, "Move cannot be null");

        Piece piece = board.getPiece(move.from());

        if (piece == null || piece.type() != PieceType.KNIGHT) {
            return false;
        }

        if (!isLShapedMove(move)) {
            return false;
        }

        return isDestinationAvailable(board, move, piece);
    }

    private boolean isLShapedMove(Move move) {
        int fileDifference = Math.abs(
                move.to().file() - move.from().file()
        );

        int rankDifference = Math.abs(
                move.to().rank() - move.from().rank()
        );

        return (fileDifference == 2 && rankDifference == 1)
                || (fileDifference == 1 && rankDifference == 2);
    }

    private boolean isDestinationAvailable(Board board, Move move, Piece piece) {
        Piece targetPiece = board.getPiece(move.to());

        return targetPiece == null
                || targetPiece.color() != piece.color();
    }
}
