
package com.castleops.chess.engine;

import com.castleops.chess.model.*;

import java.util.Objects;

public class MoveValidator {

    public boolean isValidPawnMove(Board board, Move move) {
        Objects.requireNonNull(board, "Board cannot be null");
        Objects.requireNonNull(move, "Move cannot be null");

        Piece piece = board.getPiece(move.from());

        if (piece == null || piece.type() != PieceType.PAWN) {
            return false;
        }

        int direction = piece.color() == PieceColor.WHITE ? 1 : -1;

        int fileDifference = move.to().file() - move.from().file();
        int rankDifference = move.to().rank() - move.from().rank();

        return fileDifference == 0
                && rankDifference == direction
                && board.isEmpty(move.to());
    }
}
