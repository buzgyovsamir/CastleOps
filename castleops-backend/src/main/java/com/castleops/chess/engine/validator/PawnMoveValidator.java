
package com.castleops.chess.engine.validator;

import com.castleops.chess.model.*;

import java.util.Objects;

public class PawnMoveValidator implements PieceMoveValidator {

    @Override
    public boolean isValidMove(Board board, Move move) {
        Objects.requireNonNull(board, "Board cannot be null");
        Objects.requireNonNull(move, "Move cannot be null");

        Piece piece = board.getPiece(move.from());

        if (piece == null || piece.type() != PieceType.PAWN) {
            return false;
        }

        int direction = piece.color() == PieceColor.WHITE ? 1 : -1;

        return isSingleStepForward(board, move, direction)
                || isDoubleStepForward(board, move, piece, direction)
                || isDiagonalCapture(board, move, piece, direction);
    }

    private boolean isSingleStepForward(Board board, Move move, int direction) {
        int fileDifference = move.to().file() - move.from().file();
        int rankDifference = move.to().rank() - move.from().rank();

        return fileDifference == 0
                && rankDifference == direction
                && board.isEmpty(move.to());
    }

    private boolean isDoubleStepForward(Board board, Move move,
                                        Piece piece, int direction) {

        int startingRank = piece.color() == PieceColor.WHITE ? 1 : 6;
        int fileDifference = move.to().file() - move.from().file();
        int rankDifference = move.to().rank() - move.from().rank();

        if (move.from().rank() != startingRank
                || fileDifference != 0
                || rankDifference != 2 * direction) {
            return false;
        }

        Position intermediatePosition = new Position(
                move.from().file(),
                move.from().rank() + direction
        );

        return board.isEmpty(intermediatePosition)
                && board.isEmpty(move.to());
    }

    private boolean isDiagonalCapture(Board board, Move move,
                                      Piece piece, int direction) {

        int fileDifference = move.to().file() - move.from().file();
        int rankDifference = move.to().rank() - move.from().rank();

        if (Math.abs(fileDifference) != 1
                || rankDifference != direction) {
            return false;
        }

        Piece targetPiece = board.getPiece(move.to());

        return targetPiece != null
                && targetPiece.color() != piece.color();
    }
}
