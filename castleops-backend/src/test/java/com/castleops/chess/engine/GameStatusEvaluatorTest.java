package com.castleops.chess.engine;

import com.castleops.chess.game.GameState;
import com.castleops.chess.game.GameStatus;
import com.castleops.chess.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameStatusEvaluatorTest {

    private GameState gameState;
    private Board board;
    private GameStatusEvaluator evaluator;

    @BeforeEach
    void setUp() {
        gameState = new GameState();
        board = gameState.getBoard();
        evaluator = new GameStatusEvaluator();
    }

    @Test
    void shouldReturnActiveInInitialPosition() {
        assertEquals(GameStatus.ACTIVE, evaluator.evaluate(gameState));
    }

    @Test
    void shouldReturnActiveWhenCheckedKingCanEscape() {
        clearBoard();

        placePiece("e1", PieceType.KING, PieceColor.WHITE);
        placePiece("a8", PieceType.KING, PieceColor.BLACK);
        placePiece("e8", PieceType.ROOK, PieceColor.BLACK);

        assertEquals(GameStatus.ACTIVE, evaluator.evaluate(gameState));
    }

    @Test
    void shouldDetectCheckmateAgainstWhite() {
        clearBoard();

        placePiece("h1", PieceType.KING, PieceColor.WHITE);
        placePiece("f3", PieceType.KING, PieceColor.BLACK);
        placePiece("g2", PieceType.QUEEN, PieceColor.BLACK);

        assertEquals(GameStatus.CHECKMATE, evaluator.evaluate(gameState));
    }

    @Test
    void shouldDetectStalemateAgainstWhite() {
        clearBoard();

        placePiece("h1", PieceType.KING, PieceColor.WHITE);
        placePiece("f2", PieceType.KING, PieceColor.BLACK);
        placePiece("g3", PieceType.QUEEN, PieceColor.BLACK);

        assertEquals(GameStatus.STALEMATE, evaluator.evaluate(gameState));
    }

    @Test
    void shouldDetectCheckmateAgainstBlack() {
        clearBoard();

        placePiece("h8", PieceType.KING, PieceColor.BLACK);
        placePiece("f6", PieceType.KING, PieceColor.WHITE);
        placePiece("g7", PieceType.QUEEN, PieceColor.WHITE);
        gameState.switchTurn();

        assertEquals(GameStatus.CHECKMATE, evaluator.evaluate(gameState));
    }

    @Test
    void shouldDetectStalemateAgainstBlack() {
        clearBoard();

        placePiece("h8", PieceType.KING, PieceColor.BLACK);
        placePiece("f7", PieceType.KING, PieceColor.WHITE);
        placePiece("g6", PieceType.QUEEN, PieceColor.WHITE);
        gameState.switchTurn();

        assertEquals(GameStatus.STALEMATE, evaluator.evaluate(gameState));
    }

    private void clearBoard() {
        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                board.setPiece(new Position(file, rank), null);
            }
        }
    }

    private void placePiece(
            String square,
            PieceType type,
            PieceColor color) {

        Position position = Position.fromAlgebraic(square);
        board.setPiece(position, new Piece(type, color));

        if (type == PieceType.KING) {
            gameState.updateKingPosition(color, position);
        }
    }
}