package com.castleops.chess.engine.validator;

import com.castleops.chess.engine.CaptureResolver;
import com.castleops.chess.engine.CheckDetector;
import com.castleops.chess.game.GameState;
import com.castleops.chess.model.*;

import java.util.Objects;

public class LegalMoveValidator {

    private final MoveValidator moveValidator = new MoveValidator();
    private final CheckDetector checkDetector = new CheckDetector();
    private final PromotionValidator promotionValidator =
            new PromotionValidator();
    private final CaptureResolver captureResolver = new CaptureResolver();

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

        return keepsKingSafe(gameState, move, movingPiece);
    }

    private boolean keepsKingSafe(
            GameState gameState,
            Move move,
            Piece movingPiece) {

        Board board = gameState.getBoard();
        PieceColor movingColor = movingPiece.color();

        Position previousKingPosition =
                gameState.getKingPosition(movingColor);

        Position capturedPiecePosition =
                captureResolver.resolveCapturePosition(board, move);

        Piece capturedPiece = board.getPiece(capturedPiecePosition);

        Piece resultingPiece = move.isPromotion()
                ? new Piece(move.promotionType(), movingColor)
                : movingPiece;

        boolean movesKing = movingPiece.type() == PieceType.KING;

        try {
            board.setPiece(move.from(), null);
            board.setPiece(capturedPiecePosition, null);
            board.setPiece(move.to(), resultingPiece);

            if (movesKing) {
                gameState.updateKingPosition(movingColor, move.to());
            }

            return !checkDetector.isInCheck(gameState, movingColor);
        } finally {
            board.setPiece(move.to(), null);
            board.setPiece(move.from(), movingPiece);
            board.setPiece(capturedPiecePosition, capturedPiece);

            if (movesKing) {
                gameState.updateKingPosition(movingColor, previousKingPosition);
            }
        }
    }
}