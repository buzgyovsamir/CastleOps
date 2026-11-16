package com.castleops.chess.game;

import com.castleops.chess.model.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class AutomaticDrawBoundaryTest {
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldFinishOn150thHalfmoveWithMateTakingPrecedence(boolean mate) {
        GameState state = new GameState();
        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                state.getBoard().setPiece(new Position(file, rank), null);
            }
        }
        place(state, "f6", PieceType.KING, PieceColor.WHITE);
        place(state, "h8", PieceType.KING, PieceColor.BLACK);
        place(state, "g6", PieceType.QUEEN, PieceColor.WHITE);
        state.restoreCastlingRights(CastlingRights.none());
        state.restoreHalfmoveClock(149);
        ChessGame game = new ChessGame(state);
        assertEquals(GameStatus.ACTIVE, game.getStatus());
        // Qg7 is mate: queen covers h7/g8, and Kf6 protects g7.
        // Qg5 leaves the black king safe with available moves.
        assertTrue(game.makeMove(new Move(Position.fromAlgebraic("g6"),
                Position.fromAlgebraic(mate ? "g7" : "g5"))));
        assertEquals(150, state.getHalfmoveClock());
        assertEquals(PieceColor.BLACK, game.getCurrentTurn());
        GameResult expected = mate ? GameResult.checkmate(PieceColor.WHITE)
                : GameResult.draw(GameEndReason.SEVENTY_FIVE_MOVE_RULE);
        assertEquals(expected, game.getResult().orElseThrow());
        // After Qg5, Kh7 is legal on the board, so rejection tests the finished-game
        // guard. After Qg7#, the move is also illegal because of check.
        assertFalse(game.makeMove(new Move(Position.fromAlgebraic("h8"), Position.fromAlgebraic("h7"))));
        assertEquals(expected, game.getResult().orElseThrow());
    }

    private void place(GameState state, String square, PieceType type, PieceColor color) {
        Position position = Position.fromAlgebraic(square);
        state.getBoard().setPiece(position, new Piece(type, color));
        if (type == PieceType.KING) state.updateKingPosition(color, position);
    }
}
