package com.castleops.game;

import com.castleops.chess.model.Move;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class GameService {

    private final ConcurrentMap<UUID, GameSession> sessions =
            new ConcurrentHashMap<>();

    public GameSnapshot createGame(UUID creatorId) {
        Objects.requireNonNull(creatorId, "Creator ID cannot be null");

        while (true) {
            UUID gameId = UUID.randomUUID();
            GameSession session = new GameSession(gameId, creatorId);

            GameSession existingSession =
                    sessions.putIfAbsent(gameId, session);

            if (existingSession == null) {
                return session.getSnapshot();
            }
        }
    }

    public GameSnapshot joinGame(UUID gameId, UUID playerId) {
        Objects.requireNonNull(playerId, "Player ID cannot be null");

        return findSession(gameId).joinAndGetSnapshot(playerId);
    }

    public GameSnapshot getGame(UUID gameId) {
        return findSession(gameId).getSnapshot();
    }

    public GameSnapshot makeMove(
            UUID gameId,
            UUID playerId,
            Move move
    ) {
        Objects.requireNonNull(playerId, "Player ID cannot be null");
        Objects.requireNonNull(move, "Move cannot be null");

        return findSession(gameId).makeMoveAndGetSnapshot(playerId, move);
    }

    private GameSession findSession(UUID gameId) {
        Objects.requireNonNull(gameId, "Game ID cannot be null");

        GameSession session = sessions.get(gameId);

        if (session == null) {
            throw new GameNotFoundException(gameId);
        }

        return session;
    }
}