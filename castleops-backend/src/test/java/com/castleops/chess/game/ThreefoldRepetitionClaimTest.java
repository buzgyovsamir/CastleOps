package com.castleops.chess.game;

import com.castleops.chess.model.Move;
import com.castleops.chess.model.Piece;
import com.castleops.chess.model.PieceColor;
import com.castleops.chess.model.Position;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ThreefoldRepetitionClaimTest {

    @Test
    void shouldAllowClaimAfterThirdOccurrence() {
        ChessGame game = new ChessGame();

        repeatKnightCycle(game);
        repeatKnightCycle(game);

        assertTrue(
                game.claimThreefoldRepetitionDraw(PieceColor.WHITE)
        );
        assertEquals(GameStatus.DRAW, game.getStatus());
    }

    @Test
    void shouldRejectClaimBeforeThirdOccurrence() {
        ChessGame game = new ChessGame();

        repeatKnightCycle(game);

        assertFalse(
                game.claimThreefoldRepetitionDraw(PieceColor.WHITE)
        );
        assertEquals(GameStatus.ACTIVE, game.getStatus());
    }

    @Test
    void shouldRejectClaimFromPlayerWhoIsNotOnMove() {
        ChessGame game = new ChessGame();

        repeatKnightCycle(game);
        repeatKnightCycle(game);

        assertFalse(
                game.claimThreefoldRepetitionDraw(PieceColor.BLACK)
        );
        assertEquals(GameStatus.ACTIVE, game.getStatus());
    }

    @Test
    void shouldAllowClaimWithIntendedMoveWithoutExecutingIt() {
        ChessGame game = new ChessGame();

        repeatKnightCycle(game);

        play(game, "g1", "f3");
        play(game, "g8", "f6");
        play(game, "f3", "g1");

        Piece knightBeforeClaim = game.getPiece(position("f6"));

        assertTrue(
                game.claimThreefoldRepetitionDraw(
                        PieceColor.BLACK,
                        move("f6", "g8")
                )
        );

        assertEquals(GameStatus.DRAW, game.getStatus());
        assertEquals(PieceColor.BLACK, game.getCurrentTurn());
        assertEquals(
                knightBeforeClaim,
                game.getPiece(position("f6"))
        );
        assertNull(game.getPiece(position("g8")));
    }

    @Test
    void shouldNotRecordPositionsFromRejectedClaims() {
        ChessGame game = new ChessGame();

        play(game, "g1", "f3");
        play(game, "g8", "f6");
        play(game, "f3", "g1");

        Piece knightBeforeClaim = game.getPiece(position("f6"));

        // Returning to the initial position would only be its second occurrence.
        for (int attempt = 0; attempt < 4; attempt++) {
            assertFalse(
                    game.claimThreefoldRepetitionDraw(
                            PieceColor.BLACK,
                            move("f6", "g8")
                    )
            );
        }

        assertEquals(GameStatus.ACTIVE, game.getStatus());
        assertEquals(PieceColor.BLACK, game.getCurrentTurn());
        assertEquals(
                knightBeforeClaim,
                game.getPiece(position("f6"))
        );
        assertNull(game.getPiece(position("g8")));

        play(game, "f6", "g8");

        assertFalse(
                game.claimThreefoldRepetitionDraw(PieceColor.WHITE)
        );

        repeatKnightCycle(game);

        assertTrue(
                game.claimThreefoldRepetitionDraw(PieceColor.WHITE)
        );
    }

    @Test
    void shouldRejectClaimWithIllegalIntendedMove() {
        ChessGame game = new ChessGame();

        repeatKnightCycle(game);
        repeatKnightCycle(game);

        assertFalse(
                game.claimThreefoldRepetitionDraw(
                        PieceColor.WHITE,
                        move("e2", "e5")
                )
        );

        assertEquals(GameStatus.ACTIVE, game.getStatus());
        assertEquals(PieceColor.WHITE, game.getCurrentTurn());
    }

    @Test
    void shouldRejectClaimWithPawnMove() {
        ChessGame game = new ChessGame();

        repeatKnightCycle(game);
        repeatKnightCycle(game);

        Piece pawnBeforeClaim = game.getPiece(position("e2"));

        assertFalse(
                game.claimThreefoldRepetitionDraw(
                        PieceColor.WHITE,
                        move("e2", "e4")
                )
        );

        assertEquals(pawnBeforeClaim, game.getPiece(position("e2")));
        assertNull(game.getPiece(position("e4")));
        assertEquals(PieceColor.WHITE, game.getCurrentTurn());
        assertEquals(GameStatus.ACTIVE, game.getStatus());

        // The rejected claim must preserve the existing repetition history.
        assertTrue(
                game.claimThreefoldRepetitionDraw(PieceColor.WHITE)
        );
    }

    @Test
    void shouldRejectClaimAfterGameHasEnded() {
        ChessGame game = new ChessGame();

        for (int cycle = 0; cycle < 4; cycle++) {
            repeatKnightCycle(game);
        }

        assertEquals(GameStatus.DRAW, game.getStatus());

        assertFalse(
                game.claimThreefoldRepetitionDraw(PieceColor.WHITE)
        );
        assertFalse(
                game.claimThreefoldRepetitionDraw(
                        PieceColor.WHITE,
                        move("g1", "f3")
                )
        );
    }

    private void repeatKnightCycle(ChessGame game) {
        play(game, "g1", "f3");
        play(game, "g8", "f6");
        play(game, "f3", "g1");
        play(game, "f6", "g8");
    }

    private void play(ChessGame game, String from, String to) {
        assertTrue(
                game.makeMove(move(from, to)),
                () -> "Expected legal move: " + from + " -> " + to
        );
    }

    private Move move(String from, String to) {
        return new Move(position(from), position(to));
    }

    private Position position(String square) {
        return Position.fromAlgebraic(square);
    }
}