package com.castleops.chess.engine;

import com.castleops.chess.model.*;

import java.util.Objects;

public class CaptureResolver {


    public Position resolveCapturePosition(Board board, Move move) {
        Objects.requireNonNull(board, "Board cannot be null");
        Objects.requireNonNull(move, "Move cannot be null");

        Piece movingPiece = Objects.requireNonNull(
                board.getPiece(move.from()),
                "Source square must contain a piece"
        );

        boolean movesPawn = movingPiece.type() == PieceType.PAWN;
        boolean changesFile = move.from().file() != move.to().file();
        boolean destinationIsEmpty = board.isEmpty(move.to());

        boolean isEnPassant =
                movesPawn && changesFile && destinationIsEmpty;

        if (isEnPassant) {
            return new Position(move.to().file(), move.from().rank());
        }

        return move.to();
    }
}