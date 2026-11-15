package com.castleops.chess.game;

import com.castleops.chess.model.PositionKey;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class PositionHistory {

    private final Map<PositionKey, Integer> occurrences = new HashMap<>();

    public void recordPosition(PositionKey positionKey) {
        Objects.requireNonNull(positionKey, "Position key must not be null");

        occurrences.merge(positionKey, 1, Integer::sum);
    }

    public int countOccurrences(PositionKey positionKey) {
        Objects.requireNonNull(positionKey, "Position key must not be null");

        return occurrences.getOrDefault(positionKey, 0);
    }

    public void clear() {
        occurrences.clear();
    }
}