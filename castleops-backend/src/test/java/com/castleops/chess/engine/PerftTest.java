package com.castleops.chess.engine;

import com.castleops.chess.engine.generator.LegalMoveGenerator;
import com.castleops.chess.game.GameState;
import com.castleops.chess.game.MoveExecutor;
import com.castleops.chess.model.Move;
import com.castleops.chess.model.Piece;
import com.castleops.chess.model.PieceColor;
import com.castleops.chess.model.Position;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class PerftTest {

    private final LegalMoveGenerator generator = new LegalMoveGenerator();
    private final MoveExecutor executor = new MoveExecutor();

    @ParameterizedTest(name = "Depth {0}: {1} leaf nodes")
    @CsvSource({
            "0, 1",
            "1, 20",
            "2, 400",
            "3, 8902",
            "4, 197281"
    })
    void shouldMatchInitialPositionCountsAndRestoreState(
            int depth,
            long expectedNodes) {

        GameState state = new GameState();

        Piece[][] originalBoard = snapshotBoard(state);
        PieceColor originalTurn = state.getCurrentTurn();
        var originalStatus = state.getStatus();
        int originalHalfmoveClock = state.getHalfmoveClock();
        var originalRights = state.getCastlingRights();
        Position originalEnPassantTarget = state.getEnPassantTarget();
        Position originalWhiteKing =
                state.getKingPosition(PieceColor.WHITE);
        Position originalBlackKing =
                state.getKingPosition(PieceColor.BLACK);

        long actualNodes = countNodes(state, depth);

        assertEquals(originalHalfmoveClock, state.getHalfmoveClock());

        assertEquals(expectedNodes, actualNodes);

        assertBoardEquals(originalBoard, state);
        assertEquals(originalTurn, state.getCurrentTurn());
        assertEquals(originalStatus, state.getStatus());
        assertEquals(originalRights, state.getCastlingRights());
        assertEquals(originalEnPassantTarget, state.getEnPassantTarget());
        assertEquals(
                originalWhiteKing,
                state.getKingPosition(PieceColor.WHITE)
        );
        assertEquals(
                originalBlackKing,
                state.getKingPosition(PieceColor.BLACK)
        );
    }

    private long countNodes(GameState state, int depth) {
        if (depth == 0) {
            return 1;
        }

        long nodes = 0;

        for (Move move : generator.generateLegalMoves(state)) {
            MoveExecutor.UndoState undoState = executor.apply(state, move);
            state.switchTurn();

            try {
                nodes += countNodes(state, depth - 1);
            } finally {
                state.switchTurn();
                executor.undo(state, undoState);
            }
        }

        return nodes;
    }

    private Piece[][] snapshotBoard(GameState state) {
        Piece[][] snapshot = new Piece[8][8];

        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                snapshot[rank][file] = state.getBoard().getPiece(
                        new Position(file, rank)
                );
            }
        }

        return snapshot;
    }

    private void assertBoardEquals(
            Piece[][] expected,
            GameState state) {

        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                Position position = new Position(file, rank);

                assertEquals(
                        expected[rank][file],
                        state.getBoard().getPiece(position),
                        "Board changed at " + position.toAlgebraic()
                );
            }
        }
    }
}