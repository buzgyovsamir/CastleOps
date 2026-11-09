
package com.castleops.chess.engine;

import com.castleops.chess.game.GameState;
import com.castleops.chess.model.PieceColor;

import java.util.Objects;

public class CheckDetector {

    private final AttackDetector attackDetector = new AttackDetector();

    public boolean isInCheck(GameState gameState, PieceColor kingColor) {
        Objects.requireNonNull(gameState, "Game state cannot be null");
        Objects.requireNonNull(kingColor, "King color cannot be null");

        return attackDetector.isSquareAttacked(
                gameState.getBoard(),
                gameState.getKingPosition(kingColor),
                kingColor.opposite()
        );
    }
}
