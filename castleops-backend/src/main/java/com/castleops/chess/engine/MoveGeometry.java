
package com.castleops.chess.engine;

import com.castleops.chess.model.Position;

import java.util.Objects;

public final class MoveGeometry {

    private MoveGeometry() {
    }

    public static boolean isStraight(Position from, Position to) {
        Objects.requireNonNull(from, "From cannot be null");
        Objects.requireNonNull(to, "To cannot be null");

        return !from.equals(to)
                && (from.file() == to.file()
                || from.rank() == to.rank());
    }

    public static boolean isDiagonal(Position from, Position to) {
        Objects.requireNonNull(from, "From cannot be null");
        Objects.requireNonNull(to, "To cannot be null");

        int fileDifference = Math.abs(to.file() - from.file());
        int rankDifference = Math.abs(to.rank() - from.rank());

        return fileDifference > 0
                && fileDifference == rankDifference;
    }
}
