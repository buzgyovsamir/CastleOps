package com.castleops.chess.model;

import java.util.Objects;

public record Move(
        Position from,
        Position to,
        PieceType promotionType) {

    public Move {
        Objects.requireNonNull(from, "Source position cannot be null");
        Objects.requireNonNull(to, "Destination position cannot be null");

        if (from.equals(to)) {
            throw new IllegalArgumentException(
                    "Source and destination cannot be the same"
            );
        }

        if (promotionType != null && !isAllowedPromotionType(promotionType)) {
            throw new IllegalArgumentException(
                    "Promotion must be to a queen, rook, bishop or knight"
            );
        }
    }

    public Move(Position from, Position to) {
        this(from, to, null);
    }

    public boolean isPromotion() {
        return promotionType != null;
    }

    private static boolean isAllowedPromotionType(PieceType type) {
        return switch (type) {
            case QUEEN, ROOK, BISHOP, KNIGHT -> true;
            case KING, PAWN -> false;
        };
    }
}