package com.castleops.chess.engine.generator;

import com.castleops.chess.engine.validator.LegalMoveValidator;
import com.castleops.chess.game.GameState;
import com.castleops.chess.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LegalMoveGenerator {

    private static final List<PieceType> PROMOTION_TYPES = List.of(
            PieceType.QUEEN,
            PieceType.ROOK,
            PieceType.BISHOP,
            PieceType.KNIGHT
    );

    private final LegalMoveValidator legalMoveValidator =
            new LegalMoveValidator();

    public List<Move> generateLegalMoves(GameState gameState) {
        Objects.requireNonNull(gameState, "Game state cannot be null");

        List<Move> legalMoves = new ArrayList<>();
        Board board = gameState.getBoard();
        PieceColor currentTurn = gameState.getCurrentTurn();

        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                Position from = new Position(file, rank);
                Piece piece = board.getPiece(from);

                if (piece == null || piece.color() != currentTurn) {
                    continue;
                }

                addLegalMovesForPiece(gameState, from, piece, legalMoves);
            }
        }

        return legalMoves;
    }

    private void addLegalMovesForPiece(
            GameState gameState,
            Position from,
            Piece piece,
            List<Move> legalMoves) {

        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                Position to = new Position(file, rank);

                if (from.equals(to)) {
                    continue;
                }

                if (requiresPromotion(piece, to)) {
                    for (PieceType promotionType : PROMOTION_TYPES) {
                        Move move = new Move(from, to, promotionType);
                        addIfLegal(gameState, move, legalMoves);
                    }
                } else {
                    Move move = new Move(from, to);
                    addIfLegal(gameState, move, legalMoves);
                }
            }
        }
    }

    private boolean requiresPromotion(Piece piece, Position to) {
        if (piece.type() != PieceType.PAWN) {
            return false;
        }

        int promotionRank =
                piece.color() == PieceColor.WHITE ? 7 : 0;

        return to.rank() == promotionRank;
    }

    private void addIfLegal(
            GameState gameState,
            Move move,
            List<Move> legalMoves) {

        if (legalMoveValidator.isLegalMove(gameState, move)) {
            legalMoves.add(move);
        }
    }
}