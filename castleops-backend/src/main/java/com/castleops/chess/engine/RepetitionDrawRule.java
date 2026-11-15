package com.castleops.chess.engine;

import com.castleops.chess.game.PositionHistory;
import com.castleops.chess.model.PositionKey;

public final class RepetitionDrawRule {

    private static final int CLAIM_THRESHOLD = 3;
    private static final int AUTOMATIC_DRAW_THRESHOLD = 5;

    public boolean hasReachedClaimThreshold(
            PositionHistory history,
            PositionKey positionKey
    ) {
        return history.countOccurrences(positionKey) >= CLAIM_THRESHOLD;
    }

    public boolean hasReachedAutomaticDrawThreshold(
            PositionHistory history,
            PositionKey positionKey
    ) {
        return history.countOccurrences(positionKey)
                >= AUTOMATIC_DRAW_THRESHOLD;
    }

    public boolean wouldReachClaimThreshold(
            PositionHistory history,
            PositionKey resultingPosition
    ) {
        int occurrencesAfterMove =
                history.countOccurrences(resultingPosition) + 1;

        return occurrencesAfterMove >= CLAIM_THRESHOLD;
    }
}