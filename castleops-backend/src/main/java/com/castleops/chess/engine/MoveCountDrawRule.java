package com.castleops.chess.engine;

import com.castleops.chess.game.GameState;

import java.util.Objects;

public class MoveCountDrawRule {

    private static final int FIFTY_MOVE_THRESHOLD = 100;
    private static final int SEVENTY_FIVE_MOVE_THRESHOLD = 150;

    public boolean hasReachedClaimThreshold(GameState state) {
        Objects.requireNonNull(state, "Game state cannot be null");

        return state.getHalfmoveClock() >= FIFTY_MOVE_THRESHOLD;
    }

    public boolean hasReachedAutomaticDrawThreshold(GameState state) {
        Objects.requireNonNull(state, "Game state cannot be null");

        return state.getHalfmoveClock() >= SEVENTY_FIVE_MOVE_THRESHOLD;
    }
}