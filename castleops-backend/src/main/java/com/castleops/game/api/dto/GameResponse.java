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
        List<LegalDestination> legalMoves,
        List<OccupiedSquare> pieces,
        GameResult result,
        PieceColor drawOfferBy
) {

    public GameResponse {
        legalMoves = List.copyOf(legalMoves);
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

        List<LegalDestination> legalMoves = snapshot.legalMoves()
                .stream()
                .map(move -> new LegalDestination(
                        move.from().toAlgebraic(),
                        move.to().toAlgebraic()
                ))
                .distinct()
                .toList();

        return new GameResponse(
                snapshot.gameId(),
                snapshot.version(),
                snapshot.whitePlayerId(),
                snapshot.blackPlayerId().orElse(null),
                snapshot.status(),
                snapshot.currentTurn(),
                snapshot.inCheck(),
                legalMoves,
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

    public record LegalDestination(
            String from,
            String to
    ) {
    }
}