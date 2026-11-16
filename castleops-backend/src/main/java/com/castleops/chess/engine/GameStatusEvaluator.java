package com.castleops.chess.engine;

import com.castleops.chess.engine.generator.LegalMoveGenerator;
import com.castleops.chess.game.GameEndReason;
import com.castleops.chess.game.GameResult;
import com.castleops.chess.game.GameState;
import com.castleops.chess.game.GameStatus;

import java.util.Objects;
import java.util.Optional;

public class GameStatusEvaluator {

    private final LegalMoveGenerator legalMoveGenerator =
            new LegalMoveGenerator();

    private final CheckDetector checkDetector =
            new CheckDetector();

    private final MoveCountDrawRule moveCountDrawRule =
            new MoveCountDrawRule();

    private final InsufficientMaterialDetector insufficientMaterialDetector =
            new InsufficientMaterialDetector();

    public Optional<GameResult> evaluateResult(GameState gameState) {
        Objects.requireNonNull(gameState, "Game state cannot be null");

        boolean hasLegalMoves =
                !legalMoveGenerator.generateLegalMoves(gameState).isEmpty();

        if (!hasLegalMoves) {
            boolean isInCheck = checkDetector.isInCheck(
                    gameState,
                    gameState.getCurrentTurn()
            );

            GameResult result = isInCheck
                    ? GameResult.checkmate(
                    gameState.getCurrentTurn().opposite()
            )
                    : GameResult.draw(GameEndReason.STALEMATE);

            return Optional.of(result);
        }

        if (insufficientMaterialDetector.hasInsufficientMaterial(
                gameState.getBoard()
        )) {
            return Optional.of(
                    GameResult.draw(GameEndReason.INSUFFICIENT_MATERIAL)
            );
        }

        if (moveCountDrawRule.hasReachedAutomaticDrawThreshold(gameState)) {
            return Optional.of(
                    GameResult.draw(GameEndReason.SEVENTY_FIVE_MOVE_RULE)
            );
        }

        return Optional.empty();
    }

    public GameStatus evaluate(GameState gameState) {
        return evaluateResult(gameState)
                .map(GameResult::getStatus)
                .orElse(GameStatus.ACTIVE);
    }
}