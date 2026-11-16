package com.castleops.chess.game;

import com.castleops.chess.model.Move;
import com.castleops.chess.model.PieceColor;
import com.castleops.chess.model.Position;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ChessGameLegalMovesTest {

    @Test
    void shouldReturnTwentyMovesInInitialPosition() {
        ChessGame game = new ChessGame();

        assertEquals(20, game.getLegalMoves().size());
    }

    @Test
    void shouldReturnMovesFromSelectedPawn() {
        ChessGame game = new ChessGame();

        List<Move> moves = game.getLegalMovesFrom(position("e2"));

        assertEquals(
                Set.of(
                        move("e2", "e3"),
                        move("e2", "e4")
                ),
                Set.copyOf(moves)
        );
        assertEquals(2, moves.size());
    }

    @Test
    void shouldReturnMovesFromSelectedKnight() {
        ChessGame game = new ChessGame();

        List<Move> moves = game.getLegalMovesFrom(position("g1"));

        assertEquals(
                Set.of(
                        move("g1", "f3"),
                        move("g1", "h3")
                ),
                Set.copyOf(moves)
        );
        assertEquals(2, moves.size());
    }

    @Test
    void shouldReturnNoMovesForEmptySquare() {
        ChessGame game = new ChessGame();

        assertTrue(
                game.getLegalMovesFrom(position("e4")).isEmpty()
        );
    }

    @Test
    void shouldReturnNoMovesForOpponentPiece() {
        ChessGame game = new ChessGame();

        assertTrue(
                game.getLegalMovesFrom(position("e7")).isEmpty()
        );
    }

    @Test
    void shouldReturnNoMovesForBlockedPiece() {
        ChessGame game = new ChessGame();

        assertTrue(
                game.getLegalMovesFrom(position("c1")).isEmpty()
        );
    }

    @Test
    void shouldUseCurrentPlayerAfterMove() {
        ChessGame game = new ChessGame();

        assertTrue(game.makeMove(move("e2", "e4")));

        assertTrue(
                game.getLegalMovesFrom(position("e4")).isEmpty()
        );

        assertEquals(
                Set.of(
                        move("e7", "e6"),
                        move("e7", "e5")
                ),
                Set.copyOf(
                        game.getLegalMovesFrom(position("e7"))
                )
        );
    }

    @Test
    void shouldReturnUnmodifiableMoves() {
        ChessGame game = new ChessGame();

        List<Move> allMoves = game.getLegalMoves();
        List<Move> pawnMoves =
                game.getLegalMovesFrom(position("e2"));

        assertThrows(
                UnsupportedOperationException.class,
                () -> allMoves.add(move("e2", "e4"))
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> pawnMoves.add(move("e2", "e4"))
        );
    }

    @Test
    void shouldNotChangeTurnResultOrPendingDrawOffer() {
        ChessGame game = new ChessGame();

        assertTrue(game.offerDraw(PieceColor.WHITE));

        List<Move> firstRead = game.getLegalMoves();
        List<Move> secondRead = game.getLegalMoves();

        assertEquals(Set.copyOf(firstRead), Set.copyOf(secondRead));
        assertEquals(PieceColor.WHITE, game.getCurrentTurn());
        assertEquals(GameStatus.ACTIVE, game.getStatus());
        assertTrue(game.getResult().isEmpty());

        assertEquals(
                PieceColor.WHITE,
                game.getDrawOfferBy().orElseThrow()
        );

        assertTrue(game.makeMove(move("e2", "e4")));
    }

    @Test
    void shouldNotCountMoveQueriesAsPositionRepetitions() {
        ChessGame game = new ChessGame();

        for (int query = 0; query < 6; query++) {
            game.getLegalMoves();
            game.getLegalMovesFrom(position("g1"));
        }

        assertFalse(
                game.claimThreefoldRepetitionDraw(PieceColor.WHITE)
        );

        assertEquals(GameStatus.ACTIVE, game.getStatus());
        assertTrue(game.getResult().isEmpty());
    }

    @Test
    void shouldReturnNoMovesAfterGameEnds() {
        ChessGame game = new ChessGame();

        assertTrue(game.offerDraw(PieceColor.WHITE));
        assertTrue(game.acceptDraw(PieceColor.BLACK));

        assertTrue(game.getLegalMoves().isEmpty());
        assertTrue(
                game.getLegalMovesFrom(position("e2")).isEmpty()
        );
    }

    @Test
    void shouldRejectNullPosition() {
        ChessGame game = new ChessGame();

        assertThrows(
                NullPointerException.class,
                () -> game.getLegalMovesFrom(null)
        );
    }

    private Move move(String from, String to) {
        return new Move(position(from), position(to));
    }

    private Position position(String square) {
        return Position.fromAlgebraic(square);
    }
}