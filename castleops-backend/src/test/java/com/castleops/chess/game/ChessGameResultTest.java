package com.castleops.chess.game;

import com.castleops.chess.model.Move;
import com.castleops.chess.model.PieceColor;
import com.castleops.chess.model.Position;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChessGameResultTest {

    @Test
    void shouldHaveNoResultWhileGameIsActive() {
        ChessGame game = new ChessGame();

        play(game, "e2", "e4");

        assertEquals(GameStatus.ACTIVE, game.getStatus());
        assertTrue(game.getResult().isEmpty());
    }

    @Test
    void shouldRecordCheckmateWinner() {
        ChessGame game = new ChessGame();

        play(game, "f2", "f3");
        play(game, "e7", "e5");
        play(game, "g2", "g4");
        play(game, "d8", "h4");

        assertEquals(GameStatus.CHECKMATE, game.getStatus());
        assertEquals(
                GameResult.checkmate(PieceColor.BLACK),
                game.getResult().orElseThrow()
        );

        assertFalse(game.makeMove(move("a2", "a3")));

        assertEquals(
                GameResult.checkmate(PieceColor.BLACK),
                game.getResult().orElseThrow()
        );
    }

    @Test
    void shouldRecordFivefoldRepetitionReason() {
        ChessGame game = new ChessGame();

        for (int cycle = 0; cycle < 4; cycle++) {
            repeatKnightCycle(game);
        }

        assertEquals(
                GameResult.draw(GameEndReason.FIVEFOLD_REPETITION),
                game.getResult().orElseThrow()
        );
    }

    @Test
    void shouldRecordThreefoldRepetitionClaimReason() {
        ChessGame game = new ChessGame();

        repeatKnightCycle(game);
        repeatKnightCycle(game);

        assertTrue(
                game.claimThreefoldRepetitionDraw(PieceColor.WHITE)
        );

        assertEquals(
                GameResult.draw(GameEndReason.THREEFOLD_REPETITION),
                game.getResult().orElseThrow()
        );
    }

    @Test
    void shouldNotCreateResultForRejectedClaim() {
        ChessGame game = new ChessGame();

        assertFalse(
                game.claimThreefoldRepetitionDraw(PieceColor.WHITE)
        );
        assertFalse(game.claimFiftyMoveDraw(PieceColor.WHITE));

        assertTrue(game.getResult().isEmpty());
        assertEquals(GameStatus.ACTIVE, game.getStatus());
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
        return new Move(
                Position.fromAlgebraic(from),
                Position.fromAlgebraic(to)
        );
    }
}