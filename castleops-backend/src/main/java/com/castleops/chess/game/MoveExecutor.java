package com.castleops.chess.game;

import com.castleops.chess.engine.CaptureResolver;
import com.castleops.chess.model.*;

import java.util.Objects;

/**
 * Internal mutation primitive for already validated moves on consistent states.
 * Callers require exclusive access throughout validation, application and undo.
 * Does not update turn, history, result or draw offers.
 */
public class MoveExecutor {

    private final CaptureResolver captureResolver = new CaptureResolver();


    private RookMove resolveCastlingRookMove(
            Board board,
            Move move,
            Piece movingPiece) {

        boolean movesKing = movingPiece.type() == PieceType.KING;

        int fileDistance =
                Math.abs(move.to().file() - move.from().file());

        if (!movesKing || fileDistance != 2) {
            return null;
        }

        boolean isKingSide = move.to().file() > move.from().file();
        int rank = move.from().rank();

        Position rookFrom = new Position(isKingSide ? 7 : 0, rank);
        Position rookTo = new Position(isKingSide ? 5 : 3, rank);

        Piece rook = Objects.requireNonNull(
                board.getPiece(rookFrom),
                "Validated castling move must have a rook"
        );

        return new RookMove(rookFrom, rookTo, rook);
    }

    /**
     * Applies a move validated against this unchanged state. For temporary moves,
     * callers must undo in finally. The token belongs to this application only.
     */
    public UndoState apply(GameState state, Move move) {
        Objects.requireNonNull(state, "Game state cannot be null");
        Objects.requireNonNull(move, "Move cannot be null");

        Board board = state.getBoard();

        Piece movingPiece = Objects.requireNonNull(
                board.getPiece(move.from()),
                "Source square must contain a piece"
        );

        Position capturePosition =
                captureResolver.resolveCapturePosition(board, move);

        Piece capturedPiece = board.getPiece(capturePosition);

        RookMove rookMove = resolveCastlingRookMove(
                board, move, movingPiece
        );

        UndoState undoState = new UndoState(
                move,
                movingPiece,
                capturedPiece,
                capturePosition,
                state.getKingPosition(movingPiece.color()),
                state.getEnPassantTarget(),
                state.getCastlingRights(),
                rookMove,
                state.getHalfmoveClock()
        );

        Piece resultingPiece = move.isPromotion()
                ? new Piece(move.promotionType(), movingPiece.color())
                : movingPiece;

        board.setPiece(move.from(), null);
        board.setPiece(capturePosition, null);
        board.setPiece(move.to(), resultingPiece);

        if (rookMove != null) {
            board.setPiece(rookMove.from(), null);
            board.setPiece(rookMove.to(), rookMove.rook());
        }

        if (movingPiece.type() == PieceType.KING) {
            state.updateKingPosition(movingPiece.color(), move.to());
            state.revokeCastlingRights(movingPiece.color());
        }

        if (movingPiece.type() == PieceType.ROOK) {
            revokeRookRight(state, movingPiece.color(), move.from());
        }

        if (capturedPiece != null && capturedPiece.type() == PieceType.ROOK) {
            revokeRookRight(state, capturedPiece.color(), capturePosition);
        }

        updateEnPassantTarget(state, move, movingPiece);

        updateHalfmoveClock(state, movingPiece, capturedPiece);

        return undoState;
    }

    /**
     * Restores the same state in reverse application order. Do not reuse tokens.
     * Any turn changes made by the caller must be restored separately.
     */
    public void undo(GameState state, UndoState undoState) {
        Objects.requireNonNull(state, "Game state cannot be null");
        Objects.requireNonNull(undoState, "Undo state cannot be null");

        Board board = state.getBoard();
        Move move = undoState.move();
        Piece movingPiece = undoState.movingPiece();

        board.setPiece(move.to(), null);
        board.setPiece(move.from(), movingPiece);
        board.setPiece(
                undoState.capturePosition(),
                undoState.capturedPiece()
        );

        RookMove rookMove = undoState.rookMove();

        if (rookMove != null) {
            board.setPiece(rookMove.to(), null);
            board.setPiece(rookMove.from(), rookMove.rook());
        }

        if (movingPiece.type() == PieceType.KING) {
            state.updateKingPosition(
                    movingPiece.color(),
                    undoState.previousKingPosition()
            );
        }

        state.updateEnPassantTarget(undoState.previousEnPassantTarget());
        state.restoreCastlingRights(undoState.previousCastlingRights());
        state.restoreHalfmoveClock(undoState.previousHalfmoveClock());
    }

    private void revokeRookRight(
            GameState state,
            PieceColor color,
            Position rookPosition) {

        int homeRank = color == PieceColor.WHITE ? 0 : 7;

        if (rookPosition.rank() != homeRank) {
            return;
        }

        if (rookPosition.file() == 0) {
            state.revokeCastlingRight(color, CastlingSide.QUEEN_SIDE);
        } else if (rookPosition.file() == 7) {
            state.revokeCastlingRight(color, CastlingSide.KING_SIDE);
        }
    }

    private void updateEnPassantTarget(
            GameState state,
            Move move,
            Piece movingPiece) {

        int rankDistance =
                Math.abs(move.to().rank() - move.from().rank());

        boolean isDoublePawnMove =
                movingPiece.type() == PieceType.PAWN && rankDistance == 2;

        if (!isDoublePawnMove) {
            state.updateEnPassantTarget(null);
            return;
        }

        int intermediateRank =
                (move.from().rank() + move.to().rank()) / 2;

        state.updateEnPassantTarget(new Position(
                move.from().file(),
                intermediateRank
        ));
    }

    private void updateHalfmoveClock(
            GameState state,
            Piece movingPiece,
            Piece capturedPiece) {

        boolean movesPawn = movingPiece.type() == PieceType.PAWN;
        boolean capturesPiece = capturedPiece != null;

        if (movesPawn || capturesPiece) {
            state.resetHalfmoveClock();
            return;
        }

        state.incrementHalfmoveClock();
    }

    public record UndoState(
            Move move,
            Piece movingPiece,
            Piece capturedPiece,
            Position capturePosition,
            Position previousKingPosition,
            Position previousEnPassantTarget,
            CastlingRights previousCastlingRights,
            RookMove rookMove,
            int previousHalfmoveClock) {
    }

    public record RookMove(
            Position from,
            Position to,
            Piece rook) {
    }
}
