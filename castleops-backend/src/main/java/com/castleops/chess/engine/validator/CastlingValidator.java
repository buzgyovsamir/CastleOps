package com.castleops.chess.engine.validator;

import com.castleops.chess.engine.AttackDetector;
import com.castleops.chess.game.GameState;
import com.castleops.chess.model.*;

import java.util.Objects;

public class CastlingValidator {

    private final AttackDetector attackDetector = new AttackDetector();
    private final PathValidator pathValidator = new PathValidator();

    public boolean isValidCastling(GameState state, Move move) {
        Objects.requireNonNull(state, "Game state cannot be null");
        Objects.requireNonNull(move, "Move cannot be null");

        Board board = state.getBoard();
        Piece king = board.getPiece(move.from());

        if (king == null || king.type() != PieceType.KING) {
            return false;
        }

        if (move.isPromotion()) {
            return false;
        }

        int homeRank = king.color() == PieceColor.WHITE ? 0 : 7;

        boolean startsOnHomeSquare =
                move.from().equals(new Position(4, homeRank));

        boolean endsOnHomeRank = move.to().rank() == homeRank;

        boolean isKingSide = move.to().file() == 6;
        boolean isQueenSide = move.to().file() == 2;

        if (!startsOnHomeSquare
                || !endsOnHomeRank
                || (!isKingSide && !isQueenSide)) {
            return false;
        }

        CastlingSide side = isKingSide
                ? CastlingSide.KING_SIDE
                : CastlingSide.QUEEN_SIDE;

        if (!state.getCastlingRights().allows(king.color(), side)) {
            return false;
        }

        Position rookPosition = new Position(
                isKingSide ? 7 : 0,
                homeRank
        );

        Piece rook = board.getPiece(rookPosition);

        boolean hasMatchingRook = rook != null
                && rook.type() == PieceType.ROOK
                && rook.color() == king.color();

        if (!hasMatchingRook) {
            return false;
        }

        Move pathToRook = new Move(move.from(), rookPosition);

        if (!pathValidator.isPathClear(board, pathToRook)) {
            return false;
        }

        return isKingPathSafe(board, move, king);
    }

    private boolean isKingPathSafe(Board board, Move move, Piece king) {
        PieceColor opponent = king.color().opposite();

        if (attackDetector.isSquareAttacked(board, move.from(), opponent)) {
            return false;
        }

        int direction = Integer.compare(
                move.to().file(),
                move.from().file()
        );

        Position transit = new Position(
                move.from().file() + direction,
                move.from().rank()
        );

        try {
            board.setPiece(move.from(), null);
            board.setPiece(transit, king);

            if (attackDetector.isSquareAttacked(board, transit, opponent)) {
                return false;
            }

            board.setPiece(transit, null);
            board.setPiece(move.to(), king);

            return !attackDetector.isSquareAttacked(
                    board, move.to(), opponent
            );
        } finally {
            board.setPiece(transit, null);
            board.setPiece(move.to(), null);
            board.setPiece(move.from(), king);
        }
    }
}