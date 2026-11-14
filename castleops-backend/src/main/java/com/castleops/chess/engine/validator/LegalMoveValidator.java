package com.castleops.chess.engine.validator;

import com.castleops.chess.engine.CheckDetector;
import com.castleops.chess.game.GameState;
import com.castleops.chess.game.MoveExecutor;
import com.castleops.chess.model.*;

import java.util.Objects;

public class LegalMoveValidator {

    private final MoveValidator moveValidator = new MoveValidator();
    private final CheckDetector checkDetector = new CheckDetector();
    private final PromotionValidator promotionValidator =
            new PromotionValidator();
    private final MoveExecutor moveExecutor = new MoveExecutor();

    public boolean isLegalMove(GameState gameState, Move move) {
        Objects.requireNonNull(gameState, "Game state cannot be null");
        Objects.requireNonNull(move, "Move cannot be null");

        Board board = gameState.getBoard();
        Piece movingPiece = board.getPiece(move.from());

        if (movingPiece == null) {
            return false;
        }

        if (movingPiece.color() != gameState.getCurrentTurn()) {
            return false;
        }

        Piece targetPiece = board.getPiece(move.to());

        boolean capturesKing = targetPiece != null
                && targetPiece.type() == PieceType.KING;

        if (capturesKing) {
            return false;
        }

        if (!moveValidator.isValidMove(gameState, move)) {
            return false;
        }

        if(!promotionValidator.isValidPromotion(movingPiece,move)){
            return false;
        }

        return keepsKingSafe(gameState, move);
    }

    private boolean keepsKingSafe(GameState gameState, Move move) {
        PieceColor movingColor = gameState.getCurrentTurn();

        MoveExecutor.UndoState undoState =
                moveExecutor.apply(gameState, move);

        try {
            return !checkDetector.isInCheck(gameState, movingColor);
        } finally {
            moveExecutor.undo(gameState, undoState);
        }
    }
}