package com.castleops.chess.engine.validator.pieces;

import com.castleops.chess.engine.validator.CastlingValidator;
import com.castleops.chess.engine.validator.DestinationValidator;
import com.castleops.chess.game.GameState;
import com.castleops.chess.model.*;

import java.util.Objects;

public class KingMoveValidator implements PieceMoveValidator {

    private final DestinationValidator destinationValidator =
            new DestinationValidator();

    private final CastlingValidator castlingValidator =
            new CastlingValidator();

    @Override
    public boolean isValidMove(GameState gameState, Move move) {
        Objects.requireNonNull(gameState, "Game state cannot be null");
        Objects.requireNonNull(move, "Move cannot be null");

        Board board = gameState.getBoard();
        Piece king = board.getPiece(move.from());

        if (king == null || king.type() != PieceType.KING) {
            return false;
        }

        if (isSingleStepMove(move)) {
            return destinationValidator.isDestinationAvailable(
                    board, move, king
            );
        }

        return castlingValidator.isValidCastling(gameState, move);
    }

    private boolean isSingleStepMove(Move move) {
        int fileDistance =
                Math.abs(move.to().file() - move.from().file());

        int rankDistance =
                Math.abs(move.to().rank() - move.from().rank());

        return fileDistance <= 1 && rankDistance <= 1;
    }
}