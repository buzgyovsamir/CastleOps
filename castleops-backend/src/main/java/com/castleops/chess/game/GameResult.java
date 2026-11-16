package com.castleops.chess.game;

import com.castleops.chess.model.PieceColor;

import java.util.Objects;

public record GameResult(
        GameEndReason reason,
        PieceColor winner
) {

    public GameResult {
        Objects.requireNonNull(reason, "Game end reason cannot be null");

        if (requiresWinner(reason)) {
            Objects.requireNonNull(
                    winner,
                    "A decisive result must have a winner"
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

    public static GameResult resignation(PieceColor winner) {
        return new GameResult(GameEndReason.RESIGNATION, winner);
    }

    public static GameResult draw(GameEndReason reason) {
        Objects.requireNonNull(reason, "Game end reason cannot be null");

        if (requiresWinner(reason)) {
            throw new IllegalArgumentException(
                    "A decisive reason cannot be used for a draw"
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
            case RESIGNATION -> GameStatus.RESIGNED;
            case STALEMATE -> GameStatus.STALEMATE;
            default -> GameStatus.DRAW;
        };
    }

    private static boolean requiresWinner(GameEndReason reason) {
        return reason == GameEndReason.CHECKMATE
                || reason == GameEndReason.RESIGNATION;
    }
}