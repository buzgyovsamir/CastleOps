package com.castleops.chess.game;

import com.castleops.chess.model.PieceColor;

import java.util.Objects;

public record GameResult(
        GameEndReason reason,
        PieceColor winner
) {

    public GameResult {
        Objects.requireNonNull(reason, "Game end reason must not be null");

        if (reason == GameEndReason.CHECKMATE) {
            Objects.requireNonNull(
                    winner,
                    "Checkmate must have a winner"
            );
        } else if (winner != null) {
            throw new IllegalArgumentException(
                    "A drawn game cannot have a winner"
            );
        }
    }

    public static GameResult checkmate(PieceColor winner) {
        return new GameResult(GameEndReason.CHECKMATE, winner);
    }

    public static GameResult draw(GameEndReason reason) {
        Objects.requireNonNull(reason, "Game end reason must not be null");

        if (reason == GameEndReason.CHECKMATE) {
            throw new IllegalArgumentException(
                    "Checkmate cannot be used as a draw reason"
            );
        }

        return new GameResult(reason, null);
    }

    public boolean isDraw() {
        return winner == null;
    }

    public GameStatus getStatus() {
        return switch (reason) {
            case CHECKMATE -> GameStatus.CHECKMATE;
            case STALEMATE -> GameStatus.STALEMATE;
            default -> GameStatus.DRAW;
        };
    }
}