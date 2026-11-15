package com.castleops.chess.game;

import com.castleops.chess.model.Move;
import com.castleops.chess.model.Position;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RepetitionDrawIntegrationTest {

    @Test
    void shouldRemainActiveAfterThirdOccurrence() {
        ChessGame game = new ChessGame();

        repeatKnightCycle(game);
        repeatKnightCycle(game);

        assertEquals(GameStatus.ACTIVE, game.getStatus());
    }

    @Test
    void shouldRemainActiveAfterFourthOccurrence() {
        ChessGame game = new ChessGame();

        for (int cycle = 0; cycle < 3; cycle++) {
            repeatKnightCycle(game);
        }

        assertEquals(GameStatus.ACTIVE, game.getStatus());
    }

    @Test
    void shouldAutomaticallyDrawAfterFifthOccurrence() {
        ChessGame game = new ChessGame();

        for (int cycle = 0; cycle < 4; cycle++) {
            repeatKnightCycle(game);
        }

        assertEquals(GameStatus.DRAW, game.getStatus());
    }

    @Test
    void shouldRejectMovesAfterAutomaticDraw() {
        ChessGame game = new ChessGame();

        for (int cycle = 0; cycle < 4; cycle++) {
            repeatKnightCycle(game);
        }

        assertFalse(game.makeMove(move("e2", "e4")));
    }

    @Test
    void shouldNotCountRejectedMovesAsOccurrences() {
        ChessGame game = new ChessGame();

        for (int attempt = 0; attempt < 6; attempt++) {
            assertFalse(game.makeMove(move("e2", "e5")));
        }

        for (int cycle = 0; cycle < 3; cycle++) {
            repeatKnightCycle(game);
        }

        assertEquals(GameStatus.ACTIVE, game.getStatus());

        repeatKnightCycle(game);

        assertEquals(GameStatus.DRAW, game.getStatus());
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