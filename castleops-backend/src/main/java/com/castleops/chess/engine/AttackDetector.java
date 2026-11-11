
package com.castleops.chess.engine;

import com.castleops.chess.engine.validator.PathValidator;
import com.castleops.chess.model.*;

import java.util.Objects;

public class AttackDetector {

    private final PathValidator pathValidator = new PathValidator();

    public boolean isSquareAttacked(
            Board board,
            Position target,
            PieceColor attackingColor) {

        Objects.requireNonNull(board, "Board cannot be null");
        Objects.requireNonNull(target, "Target cannot be null");
        Objects.requireNonNull(attackingColor, "Color cannot be null");

        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {

                Position from = new Position(file, rank);
                Piece piece = board.getPiece(from);

                if (piece == null || piece.color() != attackingColor) {
                    continue;
                }

                if (attacksSquare(board, from, target, piece)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean attacksSquare(
            Board board,
            Position from,
            Position target,
            Piece piece) {

        if (from.equals(target)) {
            return false;
        }

        int fileDifference = target.file() - from.file();
        int rankDifference = target.rank() - from.rank();

        int absoluteFileDifference = Math.abs(fileDifference);
        int absoluteRankDifference = Math.abs(rankDifference);

        boolean isStraight =
                MoveGeometry.isStraight(from,target);


        boolean isDiagonal =
                MoveGeometry.isDiagonal(from,target);

        return switch (piece.type()) {

            case PAWN -> {
                int direction =
                        piece.color() == PieceColor.WHITE ? 1 : -1;

                yield absoluteFileDifference == 1
                        && rankDifference == direction;
            }

            case KNIGHT ->
                    (absoluteFileDifference == 1
                            && absoluteRankDifference == 2)
                            || (absoluteFileDifference == 2
                            && absoluteRankDifference == 1);

            case KING ->
                    absoluteFileDifference <= 1
                            && absoluteRankDifference <= 1;

            case BISHOP ->
                    isDiagonal && isPathClear(board, from, target);

            case ROOK ->
                    isStraight && isPathClear(board, from, target);

            case QUEEN ->
                    (isStraight || isDiagonal)
                            && isPathClear(board, from, target);
        };
    }

    private boolean isPathClear(
            Board board,
            Position from,
            Position target) {

        return pathValidator.isPathClear(
                board,
                new Move(from, target)
        );
    }
}
