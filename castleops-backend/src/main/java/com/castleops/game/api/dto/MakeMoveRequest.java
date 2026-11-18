package com.castleops.game.api.dto;

import com.castleops.chess.model.PieceType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public record MakeMoveRequest(
        @NotNull UUID playerId,

        @NotNull
        @Pattern(regexp = "[a-h][1-8]")
        String from,

        @NotNull
        @Pattern(regexp = "[a-h][1-8]")
        String to,

        PieceType promotionType
) {
}