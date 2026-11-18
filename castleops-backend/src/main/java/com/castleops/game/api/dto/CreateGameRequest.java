package com.castleops.game.api.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateGameRequest(
        @NotNull UUID playerId
) {
}