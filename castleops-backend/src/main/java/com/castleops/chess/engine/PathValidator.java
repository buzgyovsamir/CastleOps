package com.castleops.chess.engine;

import com.castleops.chess.model.Board;
import com.castleops.chess.model.Move;
import com.castleops.chess.model.Position;

import java.util.Objects;

public class PathValidator {

    public boolean isPathClear(Board board, Move move) {
        Objects.requireNonNull(board, "Board cannot be null");
        Objects.requireNonNull(move, "Move cannot be null");

        int fileDifference = move.to().file() - move.from().file();
        int rankDifference = move.to().rank() - move.from().rank();

        boolean isStraight = fileDifference == 0 || rankDifference == 0;
        boolean isDiagonal = Math.abs(fileDifference) == Math.abs(rankDifference);

        if (!isStraight && !isDiagonal) {
            return false;
        }

        int fileDirection = Integer.compare(
                move.to().file(), move.from().file()
        );

        int rankDirection = Integer.compare(
                move.to().rank(), move.from().rank()
        );

        int currentFile = move.from().file() + fileDirection;
        int currentRank = move.from().rank() + rankDirection;

        while (currentFile != move.to().file()
                || currentRank != move.to().rank()) {

            Position currentPosition = new Position(currentFile, currentRank);

            if (!board.isEmpty(currentPosition)) {
                return false;
            }

            currentFile += fileDirection;
            currentRank += rankDirection;
        }

        return true;
    }
}