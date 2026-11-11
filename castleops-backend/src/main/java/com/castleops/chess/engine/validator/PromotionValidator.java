package com.castleops.chess.engine.validator;

import com.castleops.chess.model.*;

import java.util.Objects;

public class PromotionValidator {

    public boolean isValidPromotion(Piece movingPiece, Move move) {
        Objects.requireNonNull(movingPiece, "Moving piece cannot be null");
        Objects.requireNonNull(move, "Move cannot be null");

        boolean isPawn = movingPiece.type() == PieceType.PAWN;

        if (!isPawn) {
            return !move.isPromotion();
        }

        int promotionRank =
                movingPiece.color() == PieceColor.WHITE ? 7 : 0;

        boolean reachesPromotionRank = move.to().rank() == promotionRank;

        if (reachesPromotionRank) {
            return move.isPromotion();
        }

        return !move.isPromotion();
    }
}