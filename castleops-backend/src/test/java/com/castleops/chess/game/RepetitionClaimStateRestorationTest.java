package com.castleops.chess.game;

import com.castleops.chess.engine.PositionKeyFactory;
import com.castleops.chess.engine.validator.LegalMoveValidator;
import com.castleops.chess.model.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RepetitionClaimStateRestorationTest {

    private final PositionKeyFactory positionKeyFactory =
            new PositionKeyFactory();

    private final LegalMoveValidator legalMoveValidator =
            new LegalMoveValidator();

    @Test
    void shouldRestoreStateAfterProbingCastling() {
        GameState state = createEmptyPosition();

        place(state, "h1", PieceType.ROOK, PieceColor.WHITE);
        state.restoreCastlingRights(CastlingRights.initial());
        state.restoreHalfmoveClock(12);

        assertRejectedClaimPreservesState(
                state,
                move("e1", "g1")
        );
    }

    @Test
    void shouldRestoreStateAfterProbingEnPassant() {
        GameState state = createEmptyPosition();

        place(state, "e5", PieceType.PAWN, PieceColor.WHITE);
        place(state, "d5", PieceType.PAWN, PieceColor.BLACK);
        state.updateEnPassantTarget(position("d6"));

        assertRejectedClaimPreservesState(
                state,
                move("e5", "d6")
        );
    }

    @Test
    void shouldRestoreStateAfterProbingPromotion() {
        GameState state = createEmptyPosition();

        place(state, "e7", PieceType.PAWN, PieceColor.WHITE);
        state.restoreHalfmoveClock(8);

        Move promotion = new Move(
                position("e7"),
                position("e8"),
                PieceType.QUEEN
        );

        assertRejectedClaimPreservesState(state, promotion);
    }

    @Test
    void shouldRestoreStateAfterProbingRookCapture() {
        GameState state = createEmptyPosition();

        place(state, "h1", PieceType.ROOK, PieceColor.WHITE);
        place(state, "h8", PieceType.ROOK, PieceColor.BLACK);

        state.restoreCastlingRights(CastlingRights.initial());
        state.restoreHalfmoveClock(24);

        // Moving the white rook and capturing the black rook
        // temporarily revoke castling rights for both colors.
        assertRejectedClaimPreservesState(
                state,
                move("h1", "h8")
        );
    }

    @Test
    void shouldRestoreEnPassantTargetAfterProbingQuietMove() {
        GameState state = createEmptyPosition();

        place(state, "e5", PieceType.PAWN, PieceColor.WHITE);
        place(state, "d5", PieceType.PAWN, PieceColor.BLACK);
        place(state, "g1", PieceType.KNIGHT, PieceColor.WHITE);

        state.updateEnPassantTarget(position("d6"));

        // The knight move temporarily clears the en passant target.
        assertRejectedClaimPreservesState(
                state,
                move("g1", "f3")
        );
    }

    private void assertRejectedClaimPreservesState(
            GameState state,
            Move intendedMove
    ) {
        ChessGame game = new ChessGame(state);

        assertEquals(GameStatus.ACTIVE, game.getStatus());

        PositionKey originalPosition = positionKeyFactory.create(state);
        PieceColor originalTurn = state.getCurrentTurn();
        GameStatus originalStatus = state.getStatus();

        Position originalWhiteKing =
                state.getKingPosition(PieceColor.WHITE);
        Position originalBlackKing =
                state.getKingPosition(PieceColor.BLACK);
        Position originalEnPassantTarget =
                state.getEnPassantTarget();

        CastlingRights originalCastlingRights =
                state.getCastlingRights();
        int originalHalfmoveClock = state.getHalfmoveClock();

        // Ensure that the claim reaches the temporary-move logic.
        assertTrue(
                legalMoveValidator.isLegalMove(state, intendedMove),
                "The intended move must be legal"
        );

        assertFalse(
                game.claimThreefoldRepetitionDraw(
                        originalTurn,
                        intendedMove
                )
        );

        assertAll(
                () -> assertEquals(
                        originalPosition,
                        positionKeyFactory.create(state)
                ),
                () -> assertEquals(
                        originalTurn,
                        state.getCurrentTurn()
                ),
                () -> assertEquals(
                        originalStatus,
                        state.getStatus()
                ),
                () -> assertEquals(
                        originalWhiteKing,
                        state.getKingPosition(PieceColor.WHITE)
                ),
                () -> assertEquals(
                        originalBlackKing,
                        state.getKingPosition(PieceColor.BLACK)
                ),
                () -> assertEquals(
                        originalEnPassantTarget,
                        state.getEnPassantTarget()
                ),
                () -> assertEquals(
                        originalCastlingRights,
                        state.getCastlingRights()
                ),
                () -> assertEquals(
                        originalHalfmoveClock,
                        state.getHalfmoveClock()
                )
        );

        // The move must still be playable after the rejected claim.
        assertTrue(game.makeMove(intendedMove));
    }

    private GameState createEmptyPosition() {
        GameState state = new GameState();

        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                state.getBoard().setPiece(
                        new Position(file, rank),
                        null
                );
            }
        }

        place(state, "e1", PieceType.KING, PieceColor.WHITE);
        place(state, "a8", PieceType.KING, PieceColor.BLACK);

        state.updateKingPosition(
                PieceColor.WHITE,
                position("e1")
        );
        state.updateKingPosition(
                PieceColor.BLACK,
                position("a8")
        );

        state.restoreCastlingRights(CastlingRights.none());

        return state;
    }

    private void place(
            GameState state,
            String square,
            PieceType type,
            PieceColor color
    ) {
        state.getBoard().setPiece(
                position(square),
                new Piece(type, color)
        );
    }

    private Move move(String from, String to) {
        return new Move(position(from), position(to));
    }

    private Position position(String square) {
        return Position.fromAlgebraic(square);
    }
}