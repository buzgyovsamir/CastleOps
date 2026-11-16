package com.castleops.chess.engine;

import com.castleops.chess.model.Board;
import com.castleops.chess.model.Piece;
import com.castleops.chess.model.PieceType;
import com.castleops.chess.model.Position;
import com.castleops.chess.model.PieceColor;

import java.util.Objects;

public final class InsufficientMaterialDetector {

    public boolean hasInsufficientMaterial(Board board) {
        Objects.requireNonNull(board, "Board must not be null");

        int bishopCount = 0;
        int knightCount = 0;

        boolean hasBishopOnLightSquare = false;
        boolean hasBishopOnDarkSquare = false;

        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                Position position = new Position(file, rank);
                Piece piece = board.getPiece(position);

                if (piece == null || piece.type() == PieceType.KING) {
                    continue;
                }

                switch (piece.type()) {
                    case PAWN, ROOK, QUEEN -> {
                        return false;
                    }

                    case KNIGHT -> knightCount++;

                    case BISHOP -> {
                        bishopCount++;

                        if (isLightSquare(position)) {
                            hasBishopOnLightSquare = true;
                        } else {
                            hasBishopOnDarkSquare = true;
                        }
                    }

                    default -> {
                        // Kings were already skipped.
                    }
                }
            }
        }

        int minorPieceCount = bishopCount + knightCount;

        if (minorPieceCount <= 1) {
            return true;
        }

        boolean onlyBishopsRemain = knightCount == 0;
        boolean bishopsOccupyBothSquareColors =
                hasBishopOnLightSquare && hasBishopOnDarkSquare;

        return onlyBishopsRemain && !bishopsOccupyBothSquareColors;
    }
    public boolean hasInsufficientMaterial(
            Board board,
            PieceColor color
    ) {
        Objects.requireNonNull(board, "Board cannot be null");
        Objects.requireNonNull(color, "Color cannot be null");

        int ownMinorPieceCount = 0;
        int ownKnightCount = 0;

        boolean opponentHasPawnRookBishopOrKnight = false;
        boolean boardHasPawnOrKnight = false;
        boolean hasLightSquareBishop = false;
        boolean hasDarkSquareBishop = false;

        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                Position position = new Position(file, rank);
                Piece piece = board.getPiece(position);

                if (piece == null || piece.type() == PieceType.KING) {
                    continue;
                }

                PieceType type = piece.type();
                boolean belongsToPlayer = piece.color() == color;

                if (belongsToPlayer) {
                    if (type == PieceType.PAWN
                            || type == PieceType.ROOK
                            || type == PieceType.QUEEN) {
                        return false;
                    }

                    ownMinorPieceCount++;

                    if (type == PieceType.KNIGHT) {
                        ownKnightCount++;
                    }
                } else if (type != PieceType.QUEEN) {
                    opponentHasPawnRookBishopOrKnight = true;
                }

                if (type == PieceType.PAWN || type == PieceType.KNIGHT) {
                    boardHasPawnOrKnight = true;
                }

                if (type == PieceType.BISHOP) {
                    if (isLightSquare(position)) {
                        hasLightSquareBishop = true;
                    } else {
                        hasDarkSquareBishop = true;
                    }
                }
            }
        }

        if (ownMinorPieceCount == 0) {
            return true;
        }

        if (ownKnightCount > 0) {
            boolean hasOnlyOneKnight = ownMinorPieceCount == 1;

            return hasOnlyOneKnight
                    && !opponentHasPawnRookBishopOrKnight;
        }

        boolean bishopsOccupyBothSquareColors =
                hasLightSquareBishop && hasDarkSquareBishop;

        return !boardHasPawnOrKnight
                && !bishopsOccupyBothSquareColors;
    }


    private boolean isLightSquare(Position position) {
        return (position.file() + position.rank()) % 2 != 0;
    }
}