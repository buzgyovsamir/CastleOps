package com.castleops.chess.engine.validator.pieces;

import com.castleops.chess.model.Board;
import com.castleops.chess.model.Move;

public interface PieceMoveValidator {

    boolean isValidMove(Board board, Move move);
}