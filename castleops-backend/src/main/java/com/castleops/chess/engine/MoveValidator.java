
package com.castleops.chess.engine;

import com.castleops.chess.engine.validator.BishopMoveValidator;
import com.castleops.chess.engine.validator.KnightMoveValidator;
import com.castleops.chess.engine.validator.PawnMoveValidator;
import com.castleops.chess.engine.validator.PieceMoveValidator;
import com.castleops.chess.model.*;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public class MoveValidator {

    private final Map<PieceType, PieceMoveValidator> validators;

    public MoveValidator() {
        validators = new EnumMap<>(PieceType.class);

        validators.put(PieceType.PAWN, new PawnMoveValidator());
        validators.put(PieceType.KNIGHT, new KnightMoveValidator());
        validators.put(PieceType.BISHOP, new BishopMoveValidator());
    }

    public boolean isValidMove(Board board, Move move) {
        Objects.requireNonNull(board, "Board cannot be null");
        Objects.requireNonNull(move, "Move cannot be null");

        Piece piece = board.getPiece(move.from());

        if (piece == null) {
            return false;
        }

        PieceMoveValidator validator = validators.get(piece.type());

        if (validator == null) {
            return false;
        }

        return validator.isValidMove(board, move);
    }
}
