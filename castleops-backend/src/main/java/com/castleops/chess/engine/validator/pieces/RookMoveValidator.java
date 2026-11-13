
package com.castleops.chess.engine.validator.pieces;

import com.castleops.chess.engine.MoveGeometry;
import com.castleops.chess.engine.validator.DestinationValidator;
import com.castleops.chess.engine.validator.PathValidator;
import com.castleops.chess.game.GameState;
import com.castleops.chess.model.*;

import java.util.Objects;

public class RookMoveValidator implements PieceMoveValidator {

    private final PathValidator pathValidator = new PathValidator();
    private final DestinationValidator destinationValidator =
            new DestinationValidator();

    @Override
    public boolean isValidMove(GameState gameState, Move move) {
        Objects.requireNonNull(gameState, "Game state cannot be null");
        Objects.requireNonNull(move, "Move cannot be null");

        Board board = gameState.getBoard();
        Piece piece = board.getPiece(move.from());


        if (piece == null || piece.type() != PieceType.ROOK) {
            return false;
        }

        return MoveGeometry.isStraight(move.from(), move.to())
                && pathValidator.isPathClear(board, move)
                && destinationValidator.isDestinationAvailable(board, move, piece);
    }
}
