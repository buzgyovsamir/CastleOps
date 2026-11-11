package com.castleops.chess.engine.validator;

import com.castleops.chess.game.GameState;
import com.castleops.chess.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LegalMoveValidatorTest {

    private GameState gameState;
    private Board board;
    private LegalMoveValidator validator;

    @BeforeEach
    void setUp() {
        gameState = new GameState();
        board = gameState.getBoard();
        validator = new LegalMoveValidator();
    }

    @Test
    void shouldAllowLegalMoveWithoutChangingState() {
        Piece pawn = board.getPiece(position("e2"));

        assertTrue(validator.isLegalMove(gameState, move("e2", "e4")));

        assertEquals(pawn, board.getPiece(position("e2")));
        assertNull(board.getPiece(position("e4")));
        assertEquals(PieceColor.WHITE, gameState.getCurrentTurn());
        assertEquals(position("e1"),
                gameState.getKingPosition(PieceColor.WHITE));
    }

    @Test
    void shouldRejectMoveFromEmptySquare() {
        assertFalse(validator.isLegalMove(gameState, move("e3", "e4")));
    }

    @Test
    void shouldRejectMoveFromWrongPlayer() {
        assertFalse(validator.isLegalMove(gameState, move("e7", "e5")));
    }

    @Test
    void shouldRejectInvalidPieceMovement() {
        assertFalse(validator.isLegalMove(gameState, move("e2", "e5")));
    }

    @Test
    void shouldRejectMoveThatExposesOwnKing() {
        setUpEmptyBoardWithKings("e1", "a8");

        placePiece("e2", PieceType.ROOK, PieceColor.WHITE);
        placePiece("e8", PieceType.ROOK, PieceColor.BLACK);

        assertFalse(validator.isLegalMove(gameState, move("e2", "f2")));

        assertEquals(new Piece(PieceType.ROOK, PieceColor.WHITE),
                board.getPiece(position("e2")));
        assertNull(board.getPiece(position("f2")));
        assertEquals(PieceColor.WHITE, gameState.getCurrentTurn());
    }

    @Test
    void shouldRejectMoveThatIgnoresCheck() {
        setUpEmptyBoardWithKings("e1", "a8");

        placePiece("e8", PieceType.ROOK, PieceColor.BLACK);
        placePiece("a2", PieceType.PAWN, PieceColor.WHITE);

        assertFalse(validator.isLegalMove(gameState, move("a2", "a3")));
    }

    @Test
    void shouldAllowBlockingCheck() {
        setUpEmptyBoardWithKings("e1", "a8");

        placePiece("e8", PieceType.ROOK, PieceColor.BLACK);
        placePiece("d2", PieceType.BISHOP, PieceColor.WHITE);

        assertTrue(validator.isLegalMove(gameState, move("d2", "e3")));

        assertEquals(new Piece(PieceType.BISHOP, PieceColor.WHITE),
                board.getPiece(position("d2")));
        assertNull(board.getPiece(position("e3")));
    }

    @Test
    void shouldAllowCapturingCheckingPieceWithoutChangingBoard() {
        setUpEmptyBoardWithKings("e1", "a8");

        placePiece("e8", PieceType.ROOK, PieceColor.BLACK);
        placePiece("h5", PieceType.BISHOP, PieceColor.WHITE);

        assertTrue(validator.isLegalMove(gameState, move("h5", "e8")));

        assertEquals(new Piece(PieceType.BISHOP, PieceColor.WHITE),
                board.getPiece(position("h5")));
        assertEquals(new Piece(PieceType.ROOK, PieceColor.BLACK),
                board.getPiece(position("e8")));
    }

    @Test
    void shouldRejectKingMovingIntoAttackAndRestoreItsPosition() {
        setUpEmptyBoardWithKings("e1", "a8");
        placePiece("f8", PieceType.ROOK, PieceColor.BLACK);

        assertFalse(validator.isLegalMove(gameState, move("e1", "f1")));

        assertEquals(new Piece(PieceType.KING, PieceColor.WHITE),
                board.getPiece(position("e1")));
        assertNull(board.getPiece(position("f1")));
        assertEquals(position("e1"),
                gameState.getKingPosition(PieceColor.WHITE));
        assertEquals(PieceColor.WHITE, gameState.getCurrentTurn());
    }

    @Test
    void shouldAllowKingEscapingCheckAndRestoreItsPosition() {
        setUpEmptyBoardWithKings("e1", "a8");
        placePiece("e8", PieceType.ROOK, PieceColor.BLACK);

        assertTrue(validator.isLegalMove(gameState, move("e1", "d1")));

        assertEquals(new Piece(PieceType.KING, PieceColor.WHITE),
                board.getPiece(position("e1")));
        assertNull(board.getPiece(position("d1")));
        assertEquals(position("e1"),
                gameState.getKingPosition(PieceColor.WHITE));
        assertEquals(PieceColor.WHITE, gameState.getCurrentTurn());
    }

    @Test
    void shouldRestoreCapturedPieceAfterIllegalMove() {
        setUpEmptyBoardWithKings("e1", "a8");

        placePiece("e8", PieceType.ROOK, PieceColor.BLACK);
        placePiece("e2", PieceType.ROOK, PieceColor.WHITE);
        placePiece("f2", PieceType.KNIGHT, PieceColor.BLACK);

        assertFalse(validator.isLegalMove(gameState, move("e2", "f2")));

        assertEquals(new Piece(PieceType.ROOK, PieceColor.WHITE),
                board.getPiece(position("e2")));
        assertEquals(new Piece(PieceType.KNIGHT, PieceColor.BLACK),
                board.getPiece(position("f2")));
        assertEquals(PieceColor.WHITE, gameState.getCurrentTurn());
    }

    @Test
    void shouldRejectCapturingKing() {
        setUpEmptyBoardWithKings("a1", "e8");
        placePiece("e2", PieceType.ROOK, PieceColor.WHITE);

        assertFalse(validator.isLegalMove(gameState, move("e2", "e8")));

        assertEquals(new Piece(PieceType.KING, PieceColor.BLACK),
                board.getPiece(position("e8")));
    }

    @Test
    void shouldCheckBlackKingSafetyOnBlackTurn() {
        setUpEmptyBoardWithKings("a1", "e8");

        placePiece("e1", PieceType.ROOK, PieceColor.WHITE);
        placePiece("e7", PieceType.ROOK, PieceColor.BLACK);
        gameState.switchTurn();

        assertFalse(validator.isLegalMove(gameState, move("e7", "f7")));

        assertEquals(new Piece(PieceType.ROOK, PieceColor.BLACK),
                board.getPiece(position("e7")));
        assertNull(board.getPiece(position("f7")));
        assertEquals(PieceColor.BLACK, gameState.getCurrentTurn());
        assertEquals(position("e8"),
                gameState.getKingPosition(PieceColor.BLACK));
    }

    private void setUpEmptyBoardWithKings(
            String whiteKingSquare,
            String blackKingSquare) {

        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                board.setPiece(new Position(file, rank), null);
            }
        }

        placePiece(whiteKingSquare, PieceType.KING, PieceColor.WHITE);
        placePiece(blackKingSquare, PieceType.KING, PieceColor.BLACK);
    }

    private void placePiece(
            String square,
            PieceType type,
            PieceColor color) {

        Position position = position(square);
        board.setPiece(position, new Piece(type, color));

        if (type == PieceType.KING) {
            gameState.updateKingPosition(color, position);
        }
    }

    private Position position(String square) {
        return Position.fromAlgebraic(square);
    }

    private Move move(String from, String to) {
        return new Move(position(from), position(to));
    }
}