
package com.castleops.chess.engine.validator;

import com.castleops.chess.engine.PathValidator;
import com.castleops.chess.model.*;

import java.util.Objects;

public class BishopMoveValidator implements PieceMoveValidator {

    private final PathValidator pathValidator = new PathValidator();

    @Override
    public boolean isValidMove(Board board, Move move) {
        Objects.requireNonNull(board, "Board cannot be null");
        Objects.requireNonNull(move, "Move cannot be null");

        Piece piece = board.getPiece(move.from());

        if (piece == null || piece.type() != PieceType.BISHOP) {
            return false;
        }

        return isDiagonalMove(move)
                && pathValidator.isPathClear(board,move)
                && isDestinationAvailable(board, move, piece);
    }

    private boolean isDiagonalMove(Move move) {
        int fileDifference = Math.abs(
                move.to().file() - move.from().file()
        );

        int rankDifference = Math.abs(
                move.to().rank() - move.from().rank()
        );

        return fileDifference == rankDifference;
    }


    private boolean isDestinationAvailable(Board board, Move move, Piece piece) {
        Piece targetPiece = board.getPiece(move.to());

        return targetPiece == null
                || targetPiece.color() != piece.color();
    }
}
