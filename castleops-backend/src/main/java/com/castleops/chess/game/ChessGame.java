
package com.castleops.chess.game;

import com.castleops.chess.engine.MoveValidator;
import com.castleops.chess.model.*;

import java.util.Objects;

public class ChessGame {

    private final GameState gameState;
    private final MoveValidator moveValidator;

    public ChessGame() {
        this.gameState = new GameState();
        this.moveValidator = new MoveValidator();
    }

    public boolean makeMove(Move move) {
        Objects.requireNonNull(move, "Move cannot be null");

        if (gameState.getStatus() != GameStatus.ACTIVE) {
            return false;
        }

        Board board = gameState.getBoard();
        Piece movingPiece = board.getPiece(move.from());

        if (movingPiece == null) {
            return false;
        }

        if (movingPiece.color() != gameState.getCurrentTurn()) {
            return false;
        }

        Piece targetPiece = board.getPiece(move.to());

        if (targetPiece != null
                && targetPiece.type() == PieceType.KING) {
            return false;
        }

        if (!moveValidator.isValidMove(board, move)) {
            return false;
        }

        if (movingPiece.type() == PieceType.KING) {
            gameState.updateKingPosition(
                    movingPiece.color(),
                    move.to()
            );
        }

        board.setPiece(move.to(), movingPiece);
        board.setPiece(move.from(), null);

        gameState.switchTurn();

        return true;
    }

    public Piece getPiece(Position position) {
        return gameState.getBoard().getPiece(position);
    }

    public PieceColor getCurrentTurn() {
        return gameState.getCurrentTurn();
    }

    public GameStatus getStatus() {
        return gameState.getStatus();
    }
}
