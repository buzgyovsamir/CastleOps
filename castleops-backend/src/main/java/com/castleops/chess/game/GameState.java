
package com.castleops.chess.game;

import com.castleops.chess.model.Board;
import com.castleops.chess.model.PieceColor;
import com.castleops.chess.model.Position;

import java.util.Objects;

public class GameState {

    private final Board board;
    private PieceColor currentTurn;
    private GameStatus status;

    private Position whiteKingPosition;
    private Position blackKingPosition;

    public GameState() {
        this.board = new Board();
        this.board.setupInitialPosition();

        this.currentTurn = PieceColor.WHITE;
        this.status = GameStatus.ACTIVE;

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
        return status;
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

    void updateStatus(GameStatus status) {
        this.status = Objects.requireNonNull(
                status,
                "Game status cannot be null"
        );
    }

    public void switchTurn() {
        currentTurn = currentTurn.opposite();
    }

}
