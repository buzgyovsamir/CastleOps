
package com.castleops.chess.engine.validator;

import com.castleops.chess.engine.DestinationValidator;
import com.castleops.chess.engine.MoveGeometry;
import com.castleops.chess.engine.PathValidator;
import com.castleops.chess.model.*;

import java.util.Objects;

public class RookMoveValidator implements PieceMoveValidator {

    private final PathValidator pathValidator = new PathValidator();
    private final DestinationValidator destinationValidator =
            new DestinationValidator();

    @Override
    public boolean isValidMove(Board board, Move move) {
        Objects.requireNonNull(board, "Board cannot be null");
        Objects.requireNonNull(move, "Move cannot be null");

        Piece piece = board.getPiece(move.from());

        if (piece == null || piece.type() != PieceType.ROOK) {
            return false;
        }

        return MoveGeometry.isStraight(move.from(), move.to())
                && pathValidator.isPathClear(board, move)
                && destinationValidator.isDestinationAvailable(board, move, piece);
    }
}
