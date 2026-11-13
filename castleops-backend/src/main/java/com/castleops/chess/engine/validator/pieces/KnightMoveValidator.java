
package com.castleops.chess.engine.validator.pieces;

import com.castleops.chess.engine.validator.DestinationValidator;
import com.castleops.chess.game.GameState;
import com.castleops.chess.model.*;

import java.util.Objects;

public class KnightMoveValidator implements PieceMoveValidator {

    private final DestinationValidator destinationValidator =
            new DestinationValidator();

    @Override
    public boolean isValidMove(GameState gameState, Move move) {
        Objects.requireNonNull(gameState, "Game state cannot be null");
        Objects.requireNonNull(move, "Move cannot be null");

        Board board = gameState.getBoard();
        Piece piece = board.getPiece(move.from());

        if (piece == null || piece.type() != PieceType.KNIGHT) {
            return false;
        }

        if (!isLShapedMove(move)) {
            return false;
        }

        return destinationValidator.isDestinationAvailable(board, move, piece);
    }

    private boolean isLShapedMove(Move move) {
        int fileDifference = Math.abs(
                move.to().file() - move.from().file()
        );

        int rankDifference = Math.abs(
                move.to().rank() - move.from().rank()
        );

        return (fileDifference == 2 && rankDifference == 1)
                || (fileDifference == 1 && rankDifference == 2);
    }

//    exported logic in the MoveValidator class
//    private boolean isDestinationAvailable(Board board, Move move, Piece piece) {
//        Piece targetPiece = board.getPiece(move.to());
//
//        return targetPiece == null
//                || targetPiece.color() != piece.color();
//    }
}
