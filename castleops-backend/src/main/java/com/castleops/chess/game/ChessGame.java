package com.castleops.chess.game;

import com.castleops.chess.engine.CaptureResolver;
import com.castleops.chess.engine.GameStatusEvaluator;
import com.castleops.chess.engine.validator.LegalMoveValidator;
import com.castleops.chess.model.*;

import java.util.Objects;

public class ChessGame {

    private final GameState gameState;
    private final LegalMoveValidator legalMoveValidator;
    private final GameStatusEvaluator gameStatusEvaluator;
    private final CaptureResolver captureResolver = new CaptureResolver();

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

        Position capturedPiecePosition =
                captureResolver.resolveCapturePosition(board, move);

        Piece resultingPiece = move.isPromotion()
                ? new Piece(move.promotionType(), movingPiece.color())
                : movingPiece;

        board.setPiece(move.from(), null);
        board.setPiece(capturedPiecePosition, null);
        board.setPiece(move.to(), resultingPiece);

        if (movingPiece.type() == PieceType.KING) {
            gameState.updateKingPosition(
                    movingPiece.color(),
                    move.to()
            );
        }

        updateEnPassantTarget(move, movingPiece);
    }

    private void updateEnPassantTarget(Move move, Piece movingPiece) {
        int rankDistance = Math.abs(
                move.to().rank() - move.from().rank()
        );

        boolean isDoublePawnMove =
                movingPiece.type() == PieceType.PAWN && rankDistance == 2;

        if (!isDoublePawnMove) {
            gameState.updateEnPassantTarget(null);
            return;
        }

        int intermediateRank =
                (move.from().rank() + move.to().rank()) / 2;

        Position target = new Position(
                move.from().file(),
                intermediateRank
        );

        gameState.updateEnPassantTarget(target);
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