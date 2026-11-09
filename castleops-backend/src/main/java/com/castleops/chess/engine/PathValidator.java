
package com.castleops.chess.engine;

import com.castleops.chess.model.Board;
import com.castleops.chess.model.Move;
import com.castleops.chess.model.Position;

import java.util.Objects;

public class PathValidator {

    public boolean isPathClear(Board board, Move move) {
        Objects.requireNonNull(board, "Board cannot be null");
        Objects.requireNonNull(move, "Move cannot be null");

        Position from = move.from();
        Position to = move.to();

        if (from.equals(to)) {
            return false;
        }

        boolean isStraight = MoveGeometry.isStraight(from, to);
        boolean isDiagonal = MoveGeometry.isDiagonal(from, to);

        if (!isStraight && !isDiagonal) {
            return false;
        }

        int fileDirection = Integer.compare(
                to.file(), from.file()
        );

        int rankDirection = Integer.compare(
                to.rank(), from.rank()
        );

        int currentFile = from.file() + fileDirection;
        int currentRank = from.rank() + rankDirection;

        while (currentFile != to.file()
                || currentRank != to.rank()) {

            Position currentPosition =
                    new Position(currentFile, currentRank);

            if (!board.isEmpty(currentPosition)) {
                return false;
            }

            currentFile += fileDirection;
            currentRank += rankDirection;
        }

        return true;
    }
}
