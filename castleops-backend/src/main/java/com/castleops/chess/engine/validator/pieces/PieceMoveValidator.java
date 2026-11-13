package com.castleops.chess.engine.validator.pieces;

import com.castleops.chess.game.GameState;
import com.castleops.chess.model.Move;

public interface PieceMoveValidator {

    boolean isValidMove(GameState gameState, Move move);
}