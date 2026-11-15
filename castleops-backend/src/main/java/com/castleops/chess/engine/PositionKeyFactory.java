package com.castleops.chess.engine;

import com.castleops.chess.engine.validator.LegalMoveValidator;
import com.castleops.chess.game.GameState;
import com.castleops.chess.model.*;

import java.util.Objects;

public class PositionKeyFactory {

    private final LegalMoveValidator legalMoveValidator =
            new LegalMoveValidator();

    public PositionKey create(GameState state) {
        Objects.requireNonNull(state, "Game state cannot be null");

        return new PositionKey(
                encodeBoard(state.getBoard()),
                state.getCurrentTurn(),
                state.getCastlingRights(),
                findLegalEnPassantTarget(state)
        );
    }

    private String encodeBoard(Board board) {
        StringBuilder layout = new StringBuilder(64);

        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                Piece piece = board.getPiece(new Position(file, rank));
                layout.append(encodePiece(piece));
            }
        }

        return layout.toString();
    }

    private char encodePiece(Piece piece) {
        if (piece == null) {
            return '.';
        }

        char symbol = switch (piece.type()) {
            case KING -> 'k';
            case QUEEN -> 'q';
            case ROOK -> 'r';
            case BISHOP -> 'b';
            case KNIGHT -> 'n';
            case PAWN -> 'p';
        };

        return piece.color() == PieceColor.WHITE
                ? Character.toUpperCase(symbol)
                : symbol;
    }

    private Position findLegalEnPassantTarget(GameState state) {
        Position target = state.getEnPassantTarget();

        if (target == null || !state.getBoard().isEmpty(target)) {
            return null;
        }

        PieceColor currentTurn = state.getCurrentTurn();
        int direction = currentTurn == PieceColor.WHITE ? 1 : -1;
        int sourceRank = target.rank() - direction;

        if (sourceRank < 0 || sourceRank > 7) {
            return null;
        }

        for (int fileOffset : new int[]{-1, 1}) {
            int sourceFile = target.file() + fileOffset;

            if (sourceFile < 0 || sourceFile > 7) {
                continue;
            }

            Position from = new Position(sourceFile, sourceRank);
            Piece piece = state.getBoard().getPiece(from);

            boolean isOwnPawn = piece != null
                    && piece.type() == PieceType.PAWN
                    && piece.color() == currentTurn;

            if (!isOwnPawn) {
                continue;
            }

            Move move = new Move(from, target);

            if (legalMoveValidator.isLegalMove(state, move)) {
                return target;
            }
        }

        return null;
    }
}