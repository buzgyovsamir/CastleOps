package com.castleops.chess.support;

import com.castleops.chess.game.GameState;
import com.castleops.chess.model.Board;
import com.castleops.chess.model.Position;

public final class GameStateFixtures {

    private GameStateFixtures() {
    }

    public static GameState emptyGameState() {
        GameState gameState = new GameState();
        Board board = gameState.getBoard();

        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                board.setPiece(new Position(file, rank), null);
            }
        }

        return gameState;
    }
}