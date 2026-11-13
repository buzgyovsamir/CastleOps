package com.castleops.chess.engine.validator.pieces;

import com.castleops.chess.game.GameState;
import com.castleops.chess.model.*;

import java.util.Objects;

public class PawnMoveValidator implements PieceMoveValidator {

    @Override
    public boolean isValidMove(GameState gameState, Move move) {
        Objects.requireNonNull(gameState, "Game state cannot be null");
        Objects.requireNonNull(move, "Move cannot be null");

        Board board = gameState.getBoard();
        Piece pawn = board.getPiece(move.from());

        if (pawn == null || pawn.type() != PieceType.PAWN) {
            return false;
        }

        int direction = pawn.color() == PieceColor.WHITE ? 1 : -1;

        return isSingleStepForward(board, move, direction)
                || isDoubleStepForward(board, move, pawn, direction)
                || isDiagonalCapture(board, move, pawn, direction)
                || isEnPassantCapture(gameState, move, pawn, direction);
    }

    private boolean isSingleStepForward(
            Board board,
            Move move,
            int direction) {

        boolean staysOnSameFile =
                move.from().file() == move.to().file();

        boolean advancesOneRank =
                move.to().rank() - move.from().rank() == direction;

        return staysOnSameFile
                && advancesOneRank
                && board.isEmpty(move.to());
    }

    private boolean isDoubleStepForward(
            Board board,
            Move move,
            Piece pawn,
            int direction) {

        int startingRank = pawn.color() == PieceColor.WHITE ? 1 : 6;

        boolean startsOnInitialRank =
                move.from().rank() == startingRank;

        boolean staysOnSameFile =
                move.from().file() == move.to().file();

        boolean advancesTwoRanks =
                move.to().rank() - move.from().rank() == 2 * direction;

        if (!startsOnInitialRank || !staysOnSameFile || !advancesTwoRanks) {
            return false;
        }

        Position intermediatePosition = new Position(
                move.from().file(),
                move.from().rank() + direction
        );

        return board.isEmpty(intermediatePosition)
                && board.isEmpty(move.to());
    }

    private boolean isDiagonalCapture(
            Board board,
            Move move,
            Piece pawn,
            int direction) {

        if (!isOneStepDiagonal(move, direction)) {
            return false;
        }

        Piece targetPiece = board.getPiece(move.to());

        return targetPiece != null
                && targetPiece.color() != pawn.color();
    }

    private boolean isEnPassantCapture(
            GameState gameState,
            Move move,
            Piece pawn,
            int direction) {

        Board board = gameState.getBoard();

        boolean reachesEnPassantTarget =
                move.to().equals(gameState.getEnPassantTarget());

        if (!reachesEnPassantTarget || !board.isEmpty(move.to())) {
            return false;
        }

        int requiredRank = pawn.color() == PieceColor.WHITE ? 4 : 3;

        boolean startsOnCorrectRank = move.from().rank() == requiredRank;

        if (!startsOnCorrectRank || !isOneStepDiagonal(move, direction)) {
            return false;
        }

        Position capturedPawnPosition = new Position(
                move.to().file(),
                move.from().rank()
        );

        Piece capturedPiece = board.getPiece(capturedPawnPosition);

        return capturedPiece != null
                && capturedPiece.type() == PieceType.PAWN
                && capturedPiece.color() != pawn.color();
    }

    private boolean isOneStepDiagonal(Move move, int direction) {
        int fileDistance =
                Math.abs(move.to().file() - move.from().file());

        int rankDifference =
                move.to().rank() - move.from().rank();

        return fileDistance == 1 && rankDifference == direction;
    }
}