package com.castleops.chess.game;

import com.castleops.chess.engine.*;
import com.castleops.chess.engine.generator.LegalMoveGenerator;
import com.castleops.chess.engine.validator.LegalMoveValidator;
import com.castleops.chess.model.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
    private final LegalMoveGenerator legalMoveGenerator =
            new LegalMoveGenerator();
    private PieceColor drawOfferBy;
    private final InsufficientMaterialDetector insufficientMaterialDetector =
            new InsufficientMaterialDetector();
    private final CheckDetector checkDetector = new CheckDetector();


    public ChessGame() {
        this(new GameState());
    }

    /**
     * Takes ownership without copying. The caller must not mutate initialState
     * after construction. Intended for controlled setup; position history starts here.
     */
    ChessGame(GameState initialState) {
        this.gameState = Objects.requireNonNull(
                initialState,
                "Initial state cannot be null"
        );

        this.legalMoveValidator = new LegalMoveValidator();
        this.gameStatusEvaluator = new GameStatusEvaluator();

        positionHistory.recordPosition(
                positionKeyFactory.create(gameState)
        );

        if (gameState.getStatus() == GameStatus.ACTIVE) {
            gameStatusEvaluator.evaluateResult(gameState)
                    .ifPresent(this::finishGame);
        }
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

        boolean opponentDeclinedByMoving =
                drawOfferBy != null
                        && drawOfferBy != gameState.getCurrentTurn();

        if (opponentDeclinedByMoving) {
            drawOfferBy = null;
        }

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

        finishGame(
                GameResult.draw(GameEndReason.FIFTY_MOVE_RULE)
        );
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

        finishGame(
                GameResult.draw(GameEndReason.FIFTY_MOVE_RULE)
        );
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
        Optional<GameResult> evaluatedResult =
                gameStatusEvaluator.evaluateResult(gameState);

        if (evaluatedResult.isPresent()) {
            finishGame(evaluatedResult.get());
            return;
        }

        if (repetitionDrawRule.hasReachedAutomaticDrawThreshold(
                positionHistory,
                currentPosition
        )) {
            finishGame(
                    GameResult.draw(GameEndReason.FIVEFOLD_REPETITION)
            );
        }
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

        finishGame(
                GameResult.draw(GameEndReason.THREEFOLD_REPETITION)
        );
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

        finishGame(
                GameResult.draw(GameEndReason.THREEFOLD_REPETITION)
        );
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

    public Optional<PieceColor> getDrawOfferBy() {
        return Optional.ofNullable(drawOfferBy);
    }

    public boolean offerDraw(PieceColor player) {
        Objects.requireNonNull(player, "Player cannot be null");

        if (gameState.getStatus() != GameStatus.ACTIVE) {
            return false;
        }

        if (gameState.getCurrentTurn() != player) {
            return false;
        }

        if (drawOfferBy != null) {
            return false;
        }

        drawOfferBy = player;
        return true;
    }

    public boolean acceptDraw(PieceColor player) {
        Objects.requireNonNull(player, "Player cannot be null");

        if (!canRespondToDrawOffer(player)) {
            return false;
        }

        finishGame(GameResult.draw(GameEndReason.DRAW_AGREEMENT));
        return true;
    }

    public boolean declineDraw(PieceColor player) {
        Objects.requireNonNull(player, "Player cannot be null");

        if (!canRespondToDrawOffer(player)) {
            return false;
        }

        drawOfferBy = null;
        return true;
    }

    private boolean canRespondToDrawOffer(PieceColor player) {
        boolean gameIsActive = gameState.getStatus() == GameStatus.ACTIVE;
        boolean opponentHasOffered =
                drawOfferBy != null && drawOfferBy != player;

        return gameIsActive && opponentHasOffered;
    }

    private void finishGame(GameResult result) {
        gameState.finish(result);
        drawOfferBy = null;
    }

    public List<Move> getLegalMoves() {
        if (gameState.getStatus() != GameStatus.ACTIVE) {
            return List.of();
        }

        return List.copyOf(
                legalMoveGenerator.generateLegalMoves(gameState)
        );
    }

    public List<Move> getLegalMovesFrom(Position position) {
        Objects.requireNonNull(position, "Position cannot be null");

        Piece piece = gameState.getBoard().getPiece(position);

        boolean belongsToCurrentPlayer =
                piece != null
                        && piece.color() == gameState.getCurrentTurn();

        if (!belongsToCurrentPlayer) {
            return List.of();
        }

        return getLegalMoves().stream()
                .filter(move -> move.from().equals(position))
                .toList();
    }

    public boolean resign(PieceColor player) {
        Objects.requireNonNull(player, "Player cannot be null");

        if (gameState.getStatus() != GameStatus.ACTIVE) {
            return false;
        }

        PieceColor opponent = player.opposite();

        boolean opponentCannotMate =
                insufficientMaterialDetector.hasInsufficientMaterial(
                        gameState.getBoard(),
                        opponent
                );

        GameResult result = opponentCannotMate
                ? GameResult.draw(
                GameEndReason.RESIGNATION_WITHOUT_MATING_MATERIAL
        )
                : GameResult.resignation(opponent);

        finishGame(result);
        return true;
    }

    public Piece getPiece(Position position) {
        return gameState.getBoard().getPiece(position);
    }

    public PieceColor getCurrentTurn() {
        return gameState.getCurrentTurn();
    }

    public boolean isCurrentPlayerInCheck() {
        return checkDetector.isInCheck(gameState, getCurrentTurn());
    }

    public GameStatus getStatus() {
        return gameState.getStatus();
    }

    public Optional<GameResult> getResult() {
        return gameState.getResult();
    }

}
