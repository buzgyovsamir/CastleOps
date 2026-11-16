
package com.castleops.chess.game;

import com.castleops.chess.model.Board;
import com.castleops.chess.model.CastlingSide;
import com.castleops.chess.model.PieceColor;
import com.castleops.chess.model.Position;

import java.util.Objects;
import java.util.Optional;

/**
 * Mutable internal state owned by one ChessGame. Board access is not a snapshot.
 * Direct setup edits must keep king positions and position metadata consistent.
 * Consumers should use ChessGame rather than retain mutable state aliases.
 */
public class GameState {

    private final Board board;
    private PieceColor currentTurn;
    private GameResult result;

    private Position whiteKingPosition;
    private Position blackKingPosition;

    private Position enPassantTarget;
    private CastlingRights castlingRights = CastlingRights.initial();

    private int halfmoveClock;

    public GameState() {
        this.board = new Board();
        this.board.setupInitialPosition();

        this.currentTurn = PieceColor.WHITE;

        this.whiteKingPosition = Position.fromAlgebraic("e1");
        this.blackKingPosition = Position.fromAlgebraic("e8");
    }

    public Board getBoard() {
        return board;
    }

    public PieceColor getCurrentTurn() {
        return currentTurn;
    }

    public GameStatus getStatus() {
        return result == null
                ? GameStatus.ACTIVE
                : result.getStatus();
    }

    public Position getKingPosition(PieceColor color) {
        Objects.requireNonNull(color, "Color cannot be null");

        return color == PieceColor.WHITE
                ? whiteKingPosition
                : blackKingPosition;
    }

    public void updateKingPosition(PieceColor color, Position position) {
        Objects.requireNonNull(color, "Color cannot be null");
        Objects.requireNonNull(position, "Position cannot be null");

        if (color == PieceColor.WHITE) {
            whiteKingPosition = position;
        } else {
            blackKingPosition = position;
        }
    }

    public Optional<GameResult> getResult() {
        return Optional.ofNullable(result);
    }

    void finish(GameResult result) {
        Objects.requireNonNull(result, "Game result cannot be null");

        if (this.result != null) {
            throw new IllegalStateException("Game has already finished");
        }

        this.result = result;
    }

    public Position getEnPassantTarget() {
        return enPassantTarget;
    }

    public void updateEnPassantTarget(Position enPassantTarget) {
        this.enPassantTarget = enPassantTarget;
    }

    public CastlingRights getCastlingRights() {
        return castlingRights;
    }

    void revokeCastlingRights(PieceColor color) {
        castlingRights = castlingRights.withoutBothSides(color);
    }

    void revokeCastlingRight(PieceColor color, CastlingSide side) {
        castlingRights = castlingRights.without(color, side);
    }

    void restoreCastlingRights(CastlingRights previousRights) {
        castlingRights = Objects.requireNonNull(
                previousRights,
                "Previous castling rights cannot be null"
        );
    }

    public int getHalfmoveClock() {
        return halfmoveClock;
    }

    void resetHalfmoveClock() {
        halfmoveClock = 0;
    }

    void incrementHalfmoveClock() {
        halfmoveClock++;
    }

    void restoreHalfmoveClock(int previousValue) {
        if (previousValue < 0) {
            throw new IllegalArgumentException(
                    "Halfmove clock cannot be negative"
            );
        }

        halfmoveClock = previousValue;
    }

    public void switchTurn() {
        currentTurn = currentTurn.opposite();
    }

}
