
package com.castleops.chess.model;

import java.util.Objects;

public record Move(Position from, Position to) {

    public Move {
        Objects.requireNonNull(from, "Source position cannot be null");
        Objects.requireNonNull(to, "Destination position cannot be null");

        if (from.equals(to)) {
            throw new IllegalArgumentException(
                    "Source and destination cannot be the same"
            );
        }
    }
}
