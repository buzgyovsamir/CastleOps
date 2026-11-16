package com.castleops.chess.game;

import com.castleops.chess.model.Move;
import com.castleops.chess.model.PieceColor;
import com.castleops.chess.model.Position;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DrawAgreementTest {

    @Test
    void shouldAllowOpponentToAcceptDraw() {
        ChessGame game = new ChessGame();

        assertTrue(game.offerDraw(PieceColor.WHITE));
        assertTrue(game.acceptDraw(PieceColor.BLACK));

        assertEquals(GameStatus.DRAW, game.getStatus());
        assertEquals(
                GameResult.draw(GameEndReason.DRAW_AGREEMENT),
                game.getResult().orElseThrow()
        );
        assertTrue(game.getDrawOfferBy().isEmpty());
        assertFalse(game.makeMove(move("e2", "e4")));
    }

    @Test
    void shouldRejectAcceptingOwnOffer() {
        ChessGame game = new ChessGame();

        assertTrue(game.offerDraw(PieceColor.WHITE));
        assertFalse(game.acceptDraw(PieceColor.WHITE));

        assertEquals(GameStatus.ACTIVE, game.getStatus());
        assertEquals(
                PieceColor.WHITE,
                game.getDrawOfferBy().orElseThrow()
        );
    }

    @Test
    void shouldRejectOfferFromPlayerWhoIsNotOnMove() {
        ChessGame game = new ChessGame();

        assertFalse(game.offerDraw(PieceColor.BLACK));
        assertTrue(game.getDrawOfferBy().isEmpty());
    }

    @Test
    void shouldRejectResponseWhenThereIsNoOffer() {
        ChessGame game = new ChessGame();

        assertFalse(game.acceptDraw(PieceColor.BLACK));
        assertFalse(game.declineDraw(PieceColor.BLACK));
        assertTrue(game.getResult().isEmpty());
    }

    @Test
    void shouldKeepOfferAfterOfferingPlayerMoves() {
        ChessGame game = new ChessGame();

        assertTrue(game.offerDraw(PieceColor.WHITE));
        assertTrue(game.makeMove(move("e2", "e4")));

        assertEquals(
                PieceColor.WHITE,
                game.getDrawOfferBy().orElseThrow()
        );

        assertTrue(game.acceptDraw(PieceColor.BLACK));
    }

    @Test
    void shouldClearOfferWhenOpponentMakesLegalMove() {
        ChessGame game = new ChessGame();

        assertTrue(game.offerDraw(PieceColor.WHITE));
        assertTrue(game.makeMove(move("e2", "e4")));
        assertTrue(game.makeMove(move("e7", "e5")));

        assertTrue(game.getDrawOfferBy().isEmpty());
        assertEquals(GameStatus.ACTIVE, game.getStatus());
        assertFalse(game.acceptDraw(PieceColor.BLACK));
    }

    @Test
    void shouldKeepOfferAfterIllegalMoveAttempt() {
        ChessGame game = new ChessGame();

        assertTrue(game.offerDraw(PieceColor.WHITE));
        assertTrue(game.makeMove(move("e2", "e4")));

        assertFalse(game.makeMove(move("e7", "e4")));

        assertEquals(
                PieceColor.WHITE,
                game.getDrawOfferBy().orElseThrow()
        );
        assertTrue(game.acceptDraw(PieceColor.BLACK));
    }

    @Test
    void shouldAllowOpponentToDeclineExplicitly() {
        ChessGame game = new ChessGame();

        assertTrue(game.offerDraw(PieceColor.WHITE));
        assertTrue(game.declineDraw(PieceColor.BLACK));

        assertTrue(game.getDrawOfferBy().isEmpty());
        assertTrue(game.getResult().isEmpty());
        assertEquals(GameStatus.ACTIVE, game.getStatus());
    }

    @Test
    void shouldRejectWithdrawingOwnOfferThroughDecline() {
        ChessGame game = new ChessGame();

        assertTrue(game.offerDraw(PieceColor.WHITE));
        assertFalse(game.declineDraw(PieceColor.WHITE));

        assertEquals(
                PieceColor.WHITE,
                game.getDrawOfferBy().orElseThrow()
        );
    }

    @Test
    void shouldRejectReplacingPendingOffer() {
        ChessGame game = new ChessGame();

        assertTrue(game.offerDraw(PieceColor.WHITE));
        assertFalse(game.offerDraw(PieceColor.WHITE));

        assertTrue(game.makeMove(move("e2", "e4")));
        assertFalse(game.offerDraw(PieceColor.BLACK));

        assertEquals(
                PieceColor.WHITE,
                game.getDrawOfferBy().orElseThrow()
        );
    }

    @Test
    void shouldClearOfferWhenOfferingPlayerDeliversCheckmate() {
        ChessGame game = new ChessGame();

        assertTrue(game.makeMove(move("f2", "f3")));
        assertTrue(game.makeMove(move("e7", "e5")));
        assertTrue(game.makeMove(move("g2", "g4")));

        assertTrue(game.offerDraw(PieceColor.BLACK));
        assertTrue(game.makeMove(move("d8", "h4")));

        assertTrue(game.getDrawOfferBy().isEmpty());
        assertEquals(
                GameResult.checkmate(PieceColor.BLACK),
                game.getResult().orElseThrow()
        );
        assertFalse(game.acceptDraw(PieceColor.WHITE));
    }

    @Test
    void shouldRejectDrawActionsAfterGameEnds() {
        ChessGame game = new ChessGame();

        assertTrue(game.offerDraw(PieceColor.WHITE));
        assertTrue(game.acceptDraw(PieceColor.BLACK));

        assertFalse(game.offerDraw(PieceColor.WHITE));
        assertFalse(game.acceptDraw(PieceColor.BLACK));
        assertFalse(game.declineDraw(PieceColor.BLACK));
    }

    private Move move(String from, String to) {
        return new Move(
                Position.fromAlgebraic(from),
                Position.fromAlgebraic(to)
        );
    }
}