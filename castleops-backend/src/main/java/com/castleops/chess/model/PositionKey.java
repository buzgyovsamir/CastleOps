package com.castleops.chess.model;

import com.castleops.chess.game.CastlingRights;

import java.util.Objects;

public record PositionKey(
        String boardLayout,
        PieceColor currentTurn,
        CastlingRights castlingRights,
        Position legalEnPassantTarget) {

    public PositionKey {
        Objects.requireNonNull(boardLayout, "Board layout cannot be null");
        Objects.requireNonNull(currentTurn, "Current turn cannot be null");
        Objects.requireNonNull(castlingRights, "Castling rights cannot be null");
    }
}