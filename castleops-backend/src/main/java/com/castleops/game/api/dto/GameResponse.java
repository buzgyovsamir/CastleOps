package com.castleops.game.api.dto;

import com.castleops.chess.game.GameResult;
import com.castleops.chess.model.PieceColor;
import com.castleops.chess.model.PieceType;
import com.castleops.game.GameSessionStatus;
import com.castleops.game.GameSnapshot;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public record GameResponse(
        UUID gameId,
        long version,
        UUID whitePlayerId,
        UUID blackPlayerId,
        GameSessionStatus status,
        PieceColor currentTurn,
        boolean inCheck,
        List<OccupiedSquare> pieces,
        GameResult result,
        PieceColor drawOfferBy
) {

    public GameResponse {
        pieces = List.copyOf(pieces);
    }

    public static GameResponse from(GameSnapshot snapshot) {
        List<OccupiedSquare> pieces = snapshot.pieces()
                .entrySet()
                .stream()
                .map(entry -> new OccupiedSquare(
                        entry.getKey().toAlgebraic(),
                        entry.getValue().type(),
                        entry.getValue().color()
                ))
                .sorted(Comparator.comparing(OccupiedSquare::square))
                .toList();

        return new GameResponse(
                snapshot.gameId(),
                snapshot.version(),
                snapshot.whitePlayerId(),
                snapshot.blackPlayerId().orElse(null),
                snapshot.status(),
                snapshot.currentTurn(),
                snapshot.inCheck(),
                pieces,
                snapshot.result().orElse(null),
                snapshot.drawOfferBy().orElse(null)
        );
    }

    public record OccupiedSquare(
            String square,
            PieceType type,
            PieceColor color
    ) {
    }
}