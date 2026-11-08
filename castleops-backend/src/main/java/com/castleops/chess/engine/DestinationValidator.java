
package com.castleops.chess.engine;

import com.castleops.chess.model.Board;
import com.castleops.chess.model.Move;
import com.castleops.chess.model.Piece;

import java.util.Objects;

public class DestinationValidator {

    public boolean isDestinationAvailable(Board board, Move move, Piece piece) {
        Objects.requireNonNull(board, "Board cannot be null");
        Objects.requireNonNull(move, "Move cannot be null");
        Objects.requireNonNull(piece, "Piece cannot be null");

        Piece targetPiece = board.getPiece(move.to());

        return targetPiece == null
                || targetPiece.color() != piece.color();
    }
}
