package com.castleops.chess.game;

import com.castleops.chess.engine.GameStatusEvaluator;
import com.castleops.chess.engine.validator.LegalMoveValidator;
import com.castleops.chess.model.*;

import java.util.Objects;

public class ChessGame {

    private final GameState gameState;
    private final LegalMoveValidator legalMoveValidator;
    private final GameStatusEvaluator gameStatusEvaluator;

    public ChessGame() {
        this.gameState = new GameState();
        this.legalMoveValidator = new LegalMoveValidator();
        this.gameStatusEvaluator = new GameStatusEvaluator();
    }

    public boolean makeMove(Move move) {
        Objects.requireNonNull(move, "Move cannot be null");

        if (gameState.getStatus() != GameStatus.ACTIVE) {
            return false;
        }

        if (!legalMoveValidator.isLegalMove(gameState, move)) {
            return false;
        }

        executeMove(move);
        gameState.switchTurn();

        GameStatus status = gameStatusEvaluator.evaluate(gameState);
        gameState.updateStatus(status);

        return true;
    }

    private void executeMove(Move move) {
        Board board = gameState.getBoard();
        Piece movingPiece = board.getPiece(move.from());

        Piece resultingPiece = move.isPromotion()
                ? new Piece(move.promotionType(), movingPiece.color())
                : movingPiece;

        board.setPiece(move.from(), null);
        board.setPiece(move.to(), resultingPiece);

        if (movingPiece.type() == PieceType.KING) {
            gameState.updateKingPosition(
                    movingPiece.color(),
                    move.to()
            );
        }
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