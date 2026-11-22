package com.castleops.game;

import com.castleops.chess.game.GameResult;
import com.castleops.chess.model.Move;
import com.castleops.chess.model.Piece;
import com.castleops.chess.model.PieceColor;
import com.castleops.chess.model.Position;

import java.util.*;

public record GameSnapshot(
        UUID gameId,
        long version,
        UUID whitePlayerId,
        Optional<UUID> blackPlayerId,
        GameSessionStatus status,
        PieceColor currentTurn,
        boolean inCheck,
        List<Move> legalMoves,
        Map<Position, Piece> pieces,
        Optional<GameResult> result,
        Optional<PieceColor> drawOfferBy
) {

    public GameSnapshot {
        Objects.requireNonNull(gameId, "Game ID cannot be null");
        Objects.requireNonNull(version, "Game Version cannot be null");
        Objects.requireNonNull(whitePlayerId, "White player ID cannot be null");
        Objects.requireNonNull(blackPlayerId, "Black player optional cannot be null");
        Objects.requireNonNull(status, "Status cannot be null");
        Objects.requireNonNull(currentTurn, "Current turn cannot be null");
        legalMoves = List.copyOf(legalMoves);
        Objects.requireNonNull(pieces, "Pieces cannot be null");
        Objects.requireNonNull(result, "Result optional cannot be null");
        Objects.requireNonNull(drawOfferBy, "Draw offer optional cannot be null");

        pieces = Map.copyOf(pieces);
    }
}