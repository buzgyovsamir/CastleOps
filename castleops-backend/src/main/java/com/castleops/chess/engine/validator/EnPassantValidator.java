package com.castleops.chess.engine.validator;

import com.castleops.chess.game.GameState;
import com.castleops.chess.model.*;

import java.util.Objects;

public class EnPassantValidator {

    public boolean isValidEnPassant(GameState gameState, Move move) {
        Objects.requireNonNull(gameState, "Game state cannot be null");
        Objects.requireNonNull(move, "Move cannot be null");

        Board board = gameState.getBoard();
        Piece movingPiece = board.getPiece(move.from());

        if (movingPiece == null || movingPiece.type() != PieceType.PAWN) {
            return false;
        }

        Position enPassantTarget = gameState.getEnPassantTarget();

        if (!move.to().equals(enPassantTarget)) {
            return false;
        }

        if (!board.isEmpty(move.to()) || move.isPromotion()) {
            return false;
        }

        int direction =
                movingPiece.color() == PieceColor.WHITE ? 1 : -1;

        int requiredStartingRank =
                movingPiece.color() == PieceColor.WHITE ? 4 : 3;

        int fileDifference =
                Math.abs(move.to().file() - move.from().file());

        int rankDifference =
                move.to().rank() - move.from().rank();

        boolean startsOnCorrectRank =
                move.from().rank() == requiredStartingRank;

        boolean movesOneSquareDiagonally =
                fileDifference == 1 && rankDifference == direction;

        if (!startsOnCorrectRank || !movesOneSquareDiagonally) {
            return false;
        }

        Position capturedPawnPosition = new Position(
                move.to().file(),
                move.from().rank()
        );

        Piece capturedPiece = board.getPiece(capturedPawnPosition);

        return capturedPiece != null
                && capturedPiece.type() == PieceType.PAWN
                && capturedPiece.color() != movingPiece.color();
    }
}