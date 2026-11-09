
package com.castleops.chess.game;

import com.castleops.chess.model.Board;
import com.castleops.chess.model.Piece;
import com.castleops.chess.model.PieceColor;
import com.castleops.chess.model.PieceType;
import com.castleops.chess.model.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameStateTest {

    private GameState gameState;

    @BeforeEach
    void setUp() {
        gameState = new GameState();
    }

    @Test
    void shouldInitializeBoard() {
        Board board = gameState.getBoard();

        assertNotNull(board);

        assertEquals(
                new Piece(PieceType.KING, PieceColor.WHITE),
                board.getPiece(Position.fromAlgebraic("e1"))
        );

        assertEquals(
                new Piece(PieceType.KING, PieceColor.BLACK),
                board.getPiece(Position.fromAlgebraic("e8"))
        );

        assertEquals(
                new Piece(PieceType.PAWN, PieceColor.WHITE),
                board.getPiece(Position.fromAlgebraic("e2"))
        );

        assertTrue(board.isEmpty(Position.fromAlgebraic("e4")));
    }

    @Test
    void shouldStartWithWhiteTurn() {
        assertEquals(
                PieceColor.WHITE,
                gameState.getCurrentTurn()
        );
    }

    @Test
    void shouldStartWithActiveStatus() {
        assertEquals(
                GameStatus.ACTIVE,
                gameState.getStatus()
        );
    }

    @Test
    void shouldSwitchTurnFromWhiteToBlack() {
        gameState.switchTurn();

        assertEquals(
                PieceColor.BLACK,
                gameState.getCurrentTurn()
        );
    }

    @Test
    void shouldSwitchTurnFromBlackToWhite() {
        gameState.switchTurn();
        gameState.switchTurn();

        assertEquals(
                PieceColor.WHITE,
                gameState.getCurrentTurn()
        );
    }

    @Test
    void shouldInitializeEachGameWithIndependentBoard() {
        GameState anotherGameState = new GameState();

        assertNotSame(
                gameState.getBoard(),
                anotherGameState.getBoard()
        );
    }
}
