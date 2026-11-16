package com.castleops.chess.game;

import com.castleops.chess.model.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FiftyMoveDrawClaimTest {

    @Test
    void shouldAcceptClaimAtOneHundredHalfmoves() {
        GameState state = positionWithClock(100);
        ChessGame game = new ChessGame(state);

        assertTrue(game.claimFiftyMoveDraw(PieceColor.WHITE));

        assertEquals(GameStatus.DRAW, game.getStatus());
        assertEquals(PieceColor.WHITE, game.getCurrentTurn());
        assertEquals(100, state.getHalfmoveClock());

        assertFalse(game.makeMove(move("a2", "a3")));
        assertEquals(
                GameEndReason.FIFTY_MOVE_RULE,
                game.getResult().orElseThrow().reason()
        );
    }

    @Test
    void shouldRejectClaimBelowThreshold() {
        GameState state = positionWithClock(99);
        ChessGame game = new ChessGame(state);

        assertFalse(game.claimFiftyMoveDraw(PieceColor.WHITE));

        assertEquals(GameStatus.ACTIVE, game.getStatus());
        assertEquals(99, state.getHalfmoveClock());
        assertEquals(PieceColor.WHITE, game.getCurrentTurn());
    }

    @Test
    void shouldRejectClaimFromPlayerWithoutTurn() {
        GameState state = positionWithClock(100);
        ChessGame game = new ChessGame(state);

        assertFalse(game.claimFiftyMoveDraw(PieceColor.BLACK));

        assertEquals(GameStatus.ACTIVE, game.getStatus());
        assertEquals(100, state.getHalfmoveClock());
    }

    @Test
    void shouldAcceptClaimWithIntendedQuietMove() {
        GameState state = positionWithClock(99);
        ChessGame game = new ChessGame(state);

        assertTrue(game.claimFiftyMoveDraw(
                PieceColor.WHITE,
                move("a2", "a3")
        ));

        assertEquals(GameStatus.DRAW, game.getStatus());

        // Заявеният ход не остава изпълнен върху дъската.
        assertEquals(
                new Piece(PieceType.ROOK, PieceColor.WHITE),
                game.getPiece(position("a2"))
        );
        assertNull(game.getPiece(position("a3")));
        assertEquals(99, state.getHalfmoveClock());
        assertEquals(PieceColor.WHITE, game.getCurrentTurn());
        assertEquals(
                GameEndReason.FIFTY_MOVE_RULE,
                game.getResult().orElseThrow().reason()
        );
    }

    @Test
    void shouldRejectIntendedMoveThatDoesNotReachThreshold() {
        GameState state = positionWithClock(98);
        ChessGame game = new ChessGame(state);

        assertFalse(game.claimFiftyMoveDraw(
                PieceColor.WHITE,
                move("a2", "a3")
        ));

        assertEquals(GameStatus.ACTIVE, game.getStatus());
        assertEquals(98, state.getHalfmoveClock());
        assertNotNull(game.getPiece(position("a2")));
        assertNull(game.getPiece(position("a3")));
    }

    @Test
    void shouldRejectClaimWithIntendedPawnMove() {
        GameState state = positionWithClock(99);

        state.getBoard().setPiece(
                position("b2"),
                new Piece(PieceType.PAWN, PieceColor.WHITE)
        );

        ChessGame game = new ChessGame(state);

        assertFalse(game.claimFiftyMoveDraw(
                PieceColor.WHITE,
                move("b2", "b3")
        ));

        assertEquals(GameStatus.ACTIVE, game.getStatus());
        assertEquals(99, state.getHalfmoveClock());
        assertEquals(
                new Piece(PieceType.PAWN, PieceColor.WHITE),
                game.getPiece(position("b2"))
        );
        assertNull(game.getPiece(position("b3")));
    }

    @Test
    void shouldRejectClaimWithIntendedCapture() {
        GameState state = positionWithClock(99);

        state.getBoard().setPiece(
                position("a3"),
                new Piece(PieceType.KNIGHT, PieceColor.BLACK)
        );

        ChessGame game = new ChessGame(state);

        assertFalse(game.claimFiftyMoveDraw(
                PieceColor.WHITE,
                move("a2", "a3")
        ));

        assertEquals(GameStatus.ACTIVE, game.getStatus());
        assertEquals(99, state.getHalfmoveClock());

        assertEquals(
                new Piece(PieceType.ROOK, PieceColor.WHITE),
                game.getPiece(position("a2"))
        );
        assertEquals(
                new Piece(PieceType.KNIGHT, PieceColor.BLACK),
                game.getPiece(position("a3"))
        );
    }

    @Test
    void shouldRejectClaimWithIllegalIntendedMove() {
        GameState state = positionWithClock(99);
        ChessGame game = new ChessGame(state);

        assertFalse(game.claimFiftyMoveDraw(
                PieceColor.WHITE,
                move("a2", "b3")
        ));

        assertEquals(GameStatus.ACTIVE, game.getStatus());
        assertEquals(99, state.getHalfmoveClock());
        assertNotNull(game.getPiece(position("a2")));
        assertNull(game.getPiece(position("b3")));
    }

    @Test
    void shouldRejectFurtherClaimsAfterGameEnds() {
        ChessGame game = new ChessGame(positionWithClock(100));

        assertTrue(game.claimFiftyMoveDraw(PieceColor.WHITE));
        assertFalse(game.claimFiftyMoveDraw(PieceColor.WHITE));
        assertFalse(game.claimFiftyMoveDraw(
                PieceColor.WHITE,
                move("a2", "a3")
        ));

        assertEquals(GameStatus.DRAW, game.getStatus());
        assertEquals(
                GameEndReason.FIFTY_MOVE_RULE,
                game.getResult().orElseThrow().reason()
        );
    }

    private GameState positionWithClock(int halfmoveClock) {
        GameState state = new GameState();
        Board board = state.getBoard();

        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                board.setPiece(new Position(file, rank), null);
            }
        }

        board.setPiece(
                position("e1"),
                new Piece(PieceType.KING, PieceColor.WHITE)
        );
        board.setPiece(
                position("e8"),
                new Piece(PieceType.KING, PieceColor.BLACK)
        );
        board.setPiece(
                position("a2"),
                new Piece(PieceType.ROOK, PieceColor.WHITE)
        );

        state.updateKingPosition(PieceColor.WHITE, position("e1"));
        state.updateKingPosition(PieceColor.BLACK, position("e8"));
        state.revokeCastlingRights(PieceColor.WHITE);
        state.revokeCastlingRights(PieceColor.BLACK);
        state.restoreHalfmoveClock(halfmoveClock);

        return state;
    }

    private Position position(String square) {
        return Position.fromAlgebraic(square);
    }

    private Move move(String from, String to) {
        return new Move(position(from), position(to));
    }
}