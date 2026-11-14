package com.castleops.chess.game;

import com.castleops.chess.model.CastlingSide;
import com.castleops.chess.model.PieceColor;

import java.util.Objects;

public record CastlingRights(
        boolean whiteKingSide,
        boolean whiteQueenSide,
        boolean blackKingSide,
        boolean blackQueenSide) {

    public static CastlingRights initial() {
        return new CastlingRights(true, true, true, true);
    }

    public static CastlingRights none() {
        return new CastlingRights(false, false, false, false);
    }

    public boolean allows(PieceColor color, CastlingSide side) {
        Objects.requireNonNull(color, "Color cannot be null");
        Objects.requireNonNull(side, "Castling side cannot be null");

        return switch (color) {
            case WHITE -> switch (side) {
                case KING_SIDE -> whiteKingSide;
                case QUEEN_SIDE -> whiteQueenSide;
            };
            case BLACK -> switch (side) {
                case KING_SIDE -> blackKingSide;
                case QUEEN_SIDE -> blackQueenSide;
            };
        };
    }

    public CastlingRights withoutBothSides(PieceColor color) {
        Objects.requireNonNull(color, "Color cannot be null");

        return switch (color) {
            case WHITE -> new CastlingRights(
                    false,
                    false,
                    blackKingSide,
                    blackQueenSide
            );
            case BLACK -> new CastlingRights(
                    whiteKingSide,
                    whiteQueenSide,
                    false,
                    false
            );
        };
    }

    public CastlingRights without(
            PieceColor color,
            CastlingSide side) {

        Objects.requireNonNull(color, "Color cannot be null");
        Objects.requireNonNull(side, "Castling side cannot be null");

        return switch (color) {
            case WHITE -> switch (side) {
                case KING_SIDE -> new CastlingRights(
                        false,
                        whiteQueenSide,
                        blackKingSide,
                        blackQueenSide
                );
                case QUEEN_SIDE -> new CastlingRights(
                        whiteKingSide,
                        false,
                        blackKingSide,
                        blackQueenSide
                );
            };
            case BLACK -> switch (side) {
                case KING_SIDE -> new CastlingRights(
                        whiteKingSide,
                        whiteQueenSide,
                        false,
                        blackQueenSide
                );
                case QUEEN_SIDE -> new CastlingRights(
                        whiteKingSide,
                        whiteQueenSide,
                        blackKingSide,
                        false
                );
            };
        };
    }
}