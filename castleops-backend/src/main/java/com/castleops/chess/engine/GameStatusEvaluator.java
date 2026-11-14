package com.castleops.chess.engine;

import com.castleops.chess.engine.generator.LegalMoveGenerator;
import com.castleops.chess.game.GameState;
import com.castleops.chess.game.GameStatus;

import java.util.Objects;

public class GameStatusEvaluator {

    private final LegalMoveGenerator legalMoveGenerator =
            new LegalMoveGenerator();

    private final CheckDetector checkDetector =
            new CheckDetector();

    private final MoveCountDrawRule moveCountDrawRule =
            new MoveCountDrawRule();

    public GameStatus evaluate(GameState gameState) {
        Objects.requireNonNull(gameState, "Game state cannot be null");

        boolean hasLegalMoves =
                !legalMoveGenerator.generateLegalMoves(gameState).isEmpty();

        if (!hasLegalMoves) {
            boolean isInCheck = checkDetector.isInCheck(
                    gameState,
                    gameState.getCurrentTurn()
            );

            return isInCheck
                    ? GameStatus.CHECKMATE
                    : GameStatus.STALEMATE;
        }

        if (moveCountDrawRule.hasReachedAutomaticDrawThreshold(gameState)) {
            return GameStatus.DRAW;
        }

        return GameStatus.ACTIVE;
    }
}