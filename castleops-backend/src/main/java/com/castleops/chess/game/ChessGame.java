package com.castleops.chess.game;

import com.castleops.chess.engine.GameStatusEvaluator;
import com.castleops.chess.engine.MoveCountDrawRule;
import com.castleops.chess.engine.PositionKeyFactory;
import com.castleops.chess.engine.RepetitionDrawRule;
import com.castleops.chess.engine.validator.LegalMoveValidator;
import com.castleops.chess.model.*;

import java.util.Objects;

public class ChessGame {

    private final GameState gameState;
    private final LegalMoveValidator legalMoveValidator;
    private final GameStatusEvaluator gameStatusEvaluator;
    private final MoveExecutor moveExecutor = new MoveExecutor();
    private final MoveCountDrawRule moveCountDrawRule =
            new MoveCountDrawRule();
    private final PositionHistory positionHistory = new PositionHistory();
    private final PositionKeyFactory positionKeyFactory = new PositionKeyFactory();
    private final RepetitionDrawRule repetitionDrawRule =
            new RepetitionDrawRule();

    public ChessGame() {
        this(new GameState());
    }

    ChessGame(GameState initialState) {
        this.gameState = Objects.requireNonNull(
                initialState,
                "Initial state cannot be null"
        );
        positionHistory.recordPosition(positionKeyFactory.create(gameState));
        this.legalMoveValidator = new LegalMoveValidator();
        this.gameStatusEvaluator = new GameStatusEvaluator();

        this.gameState.updateStatus(
                gameStatusEvaluator.evaluate(this.gameState)
        );
    }

    public boolean makeMove(Move move) {
        Objects.requireNonNull(move, "Move cannot be null");

        if (gameState.getStatus() != GameStatus.ACTIVE) {
            return false;
        }

        if (!legalMoveValidator.isLegalMove(gameState, move)) {
            return false;
        }

        moveExecutor.apply(gameState, move);
        gameState.switchTurn();

        PositionKey currentPosition = recordCurrentPosition();
        updateGameStatus(currentPosition);

        return true;
    }

    public boolean claimFiftyMoveDraw(PieceColor claimant) {
        Objects.requireNonNull(claimant, "Claimant cannot be null");

        if (!canClaimDraw(claimant)) {
            return false;
        }

        if (!moveCountDrawRule.hasReachedClaimThreshold(gameState)) {
            return false;
        }

        gameState.updateStatus(GameStatus.DRAW);
        return true;
    }

    public boolean claimFiftyMoveDraw(
            PieceColor claimant,
            Move intendedMove) {

        Objects.requireNonNull(claimant, "Claimant cannot be null");
        Objects.requireNonNull(intendedMove, "Intended move cannot be null");

        if (!canClaimDraw(claimant)) {
            return false;
        }

        if (!legalMoveValidator.isLegalMove(gameState, intendedMove)) {
            return false;
        }

        if (!wouldReachFiftyMoveThreshold(intendedMove)) {
            return false;
        }

        gameState.updateStatus(GameStatus.DRAW);
        return true;
    }

    private boolean canClaimDraw(PieceColor claimant) {
        boolean gameIsActive = gameState.getStatus() == GameStatus.ACTIVE;
        boolean claimantHasTurn = gameState.getCurrentTurn() == claimant;

        return gameIsActive && claimantHasTurn;
    }

    private boolean wouldReachFiftyMoveThreshold(Move intendedMove) {
        MoveExecutor.UndoState undoState =
                moveExecutor.apply(gameState, intendedMove);

        try {
            return moveCountDrawRule.hasReachedClaimThreshold(gameState);
        } finally {
            moveExecutor.undo(gameState, undoState);
        }
    }

    private PositionKey recordCurrentPosition() {
        boolean previousPositionsCannotRepeat =
                gameState.getHalfmoveClock() == 0;

        if (previousPositionsCannotRepeat) {
            positionHistory.clear();
        }

        PositionKey positionKey = positionKeyFactory.create(gameState);
        positionHistory.recordPosition(positionKey);

        return positionKey;
    }

    private void updateGameStatus(PositionKey currentPosition) {
        GameStatus evaluatedStatus = gameStatusEvaluator.evaluate(gameState);

        if (evaluatedStatus == GameStatus.ACTIVE
                && repetitionDrawRule.hasReachedAutomaticDrawThreshold(
                positionHistory,
                currentPosition
        )) {
            evaluatedStatus = GameStatus.DRAW;
        }

        gameState.updateStatus(evaluatedStatus);
    }

    public boolean claimThreefoldRepetitionDraw(PieceColor claimant) {
        Objects.requireNonNull(claimant, "Claimant must not be null");

        if (!canClaimDraw(claimant)) {
            return false;
        }

        PositionKey currentPosition = positionKeyFactory.create(gameState);

        if (!repetitionDrawRule.hasReachedClaimThreshold(
                positionHistory,
                currentPosition
        )) {
            return false;
        }

        gameState.updateStatus(GameStatus.DRAW);
        return true;
    }

    public boolean claimThreefoldRepetitionDraw(
            PieceColor claimant,
            Move intendedMove
    ) {
        Objects.requireNonNull(claimant, "Claimant must not be null");
        Objects.requireNonNull(intendedMove, "Intended move must not be null");

        if (!canClaimDraw(claimant)) {
            return false;
        }

        if (!legalMoveValidator.isLegalMove(gameState, intendedMove)) {
            return false;
        }

        if (!wouldReachThreefoldRepetition(intendedMove)) {
            return false;
        }

        gameState.updateStatus(GameStatus.DRAW);
        return true;
    }

    private boolean wouldReachThreefoldRepetition(Move intendedMove) {
        MoveExecutor.UndoState undoState =
                moveExecutor.apply(gameState, intendedMove);

        gameState.switchTurn();

        try {
            boolean previousPositionsCannotRepeat =
                    gameState.getHalfmoveClock() == 0;

            if (previousPositionsCannotRepeat) {
                return false;
            }

            PositionKey resultingPosition =
                    positionKeyFactory.create(gameState);

            return repetitionDrawRule.wouldReachClaimThreshold(
                    positionHistory,
                    resultingPosition
            );
        } finally {
            gameState.switchTurn();
            moveExecutor.undo(gameState, undoState);
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