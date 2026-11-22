package com.castleops.game;

import com.castleops.chess.game.ChessGame;
import com.castleops.chess.game.GameStatus;
import com.castleops.chess.model.PieceColor;
import com.castleops.chess.model.Move;
import com.castleops.chess.model.Piece;
import com.castleops.chess.model.Position;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class GameSession {

    private final UUID id;
    private final UUID whitePlayerId;
    private final ChessGame chessGame;
    private long version = 0;

    private UUID blackPlayerId;

    public GameSession(UUID id, UUID whitePlayerId) {
        this.id = Objects.requireNonNull(
                id, "Game ID cannot be null"
        );
        this.whitePlayerId = Objects.requireNonNull(
                whitePlayerId, "White player ID cannot be null"
        );
        this.chessGame = new ChessGame();
    }

    public synchronized void join(UUID playerId) {
        Objects.requireNonNull(playerId, "Player ID cannot be null");

        boolean isCreator = whitePlayerId.equals(playerId);

        if (isCreator) {
            throw new IllegalArgumentException(
                    "The creator cannot join as the opponent"
            );
        }

        if (getStatus() != GameSessionStatus.WAITING_FOR_OPPONENT) {
            throw new IllegalStateException(
                    "The game is not waiting for an opponent"
            );
        }

        blackPlayerId = playerId;
        version++;
    }

    public synchronized boolean makeMove(UUID playerId, Move move) {
        Objects.requireNonNull(playerId, "Player ID cannot be null");
        Objects.requireNonNull(move, "Move cannot be null");

        if (getStatus() != GameSessionStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                    "Moves are allowed only while the game is in progress"
            );
        }

        PieceColor playerColor = getPlayerColor(playerId);

        boolean isPlayersTurn =
                chessGame.getCurrentTurn() == playerColor;

        if (!isPlayersTurn) {
            throw new IllegalStateException(
                    "It is not this player's turn"
            );
        }

        boolean moveAccepted = chessGame.makeMove(move);

        if (moveAccepted) {
            version++;
        }

        return moveAccepted;
    }

    public synchronized GameSnapshot getSnapshot() {
        Map<Position, Piece> pieces = new HashMap<>();

        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                Position position = new Position(file, rank);
                Piece piece = chessGame.getPiece(position);

                if (piece != null) {
                    pieces.put(position, piece);
                }
            }
        }

        return new GameSnapshot(
                id,
                version,
                whitePlayerId,
                Optional.ofNullable(blackPlayerId),
                getStatus(),
                chessGame.getCurrentTurn(),
                chessGame.isCurrentPlayerInCheck(),
                pieces,
                chessGame.getResult(),
                chessGame.getDrawOfferBy()
        );
    }

    public synchronized GameSnapshot makeMoveAndGetSnapshot(
            UUID playerId,
            Move move
    ) {
        boolean moveAccepted = makeMove(playerId, move);

        if (!moveAccepted) {
            throw new IllegalArgumentException("The move is not legal");
        }

        return getSnapshot();
    }

    private PieceColor getPlayerColor(UUID playerId) {
        if (whitePlayerId.equals(playerId)) {
            return PieceColor.WHITE;
        }

        if (playerId.equals(blackPlayerId)) {
            return PieceColor.BLACK;
        }

        throw new IllegalArgumentException(
                "The player is not a participant in this game"
        );
    }

    public UUID getId() {
        return id;
    }

    public UUID getWhitePlayerId() {
        return whitePlayerId;
    }

    public synchronized Optional<UUID> getBlackPlayerId() {
        return Optional.ofNullable(blackPlayerId);
    }

    public synchronized GameSessionStatus getStatus() {
        if (chessGame.getStatus() != GameStatus.ACTIVE) {
            return GameSessionStatus.FINISHED;
        }

        if (blackPlayerId == null) {
            return GameSessionStatus.WAITING_FOR_OPPONENT;
        }

        return GameSessionStatus.IN_PROGRESS;
    }

    public synchronized GameSnapshot joinAndGetSnapshot(UUID playerId) {
        join(playerId);
        return getSnapshot();
    }
}