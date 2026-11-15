package com.castleops.chess.game;

import com.castleops.chess.model.PieceColor;
import com.castleops.chess.model.PositionKey;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PositionHistoryTest {

    @Test
    void shouldReturnZeroForUnrecordedPosition() {
        PositionHistory history = new PositionHistory();
        PositionKey position = createPosition(PieceColor.WHITE);

        assertEquals(0, history.countOccurrences(position));
    }

    @Test
    void shouldCountFirstOccurrence() {
        PositionHistory history = new PositionHistory();
        PositionKey position = createPosition(PieceColor.WHITE);

        history.recordPosition(position);

        assertEquals(1, history.countOccurrences(position));
    }

    @Test
    void shouldCountRepeatedPositions() {
        PositionHistory history = new PositionHistory();
        PositionKey position = createPosition(PieceColor.WHITE);

        history.recordPosition(position);
        history.recordPosition(position);
        history.recordPosition(position);

        assertEquals(3, history.countOccurrences(position));
    }

    @Test
    void shouldTreatEqualKeysAsTheSamePosition() {
        PositionHistory history = new PositionHistory();

        PositionKey firstKey = createPosition(PieceColor.WHITE);
        PositionKey equalKey = createPosition(PieceColor.WHITE);

        history.recordPosition(firstKey);
        history.recordPosition(equalKey);

        assertEquals(2, history.countOccurrences(firstKey));
        assertEquals(2, history.countOccurrences(equalKey));
    }

    @Test
    void shouldCountDifferentPositionsIndependently() {
        PositionHistory history = new PositionHistory();

        PositionKey whiteToMove = createPosition(PieceColor.WHITE);
        PositionKey blackToMove = createPosition(PieceColor.BLACK);

        history.recordPosition(whiteToMove);
        history.recordPosition(blackToMove);
        history.recordPosition(whiteToMove);

        assertEquals(2, history.countOccurrences(whiteToMove));
        assertEquals(1, history.countOccurrences(blackToMove));
    }

    @Test
    void shouldKeepHistoriesOfDifferentGamesIndependent() {
        PositionHistory firstGameHistory = new PositionHistory();
        PositionHistory secondGameHistory = new PositionHistory();
        PositionKey position = createPosition(PieceColor.WHITE);

        firstGameHistory.recordPosition(position);

        assertEquals(1, firstGameHistory.countOccurrences(position));
        assertEquals(0, secondGameHistory.countOccurrences(position));
    }

    @Test
    void shouldRejectRecordingNullPosition() {
        PositionHistory history = new PositionHistory();

        assertThrows(
                NullPointerException.class,
                () -> history.recordPosition(null)
        );
    }

    @Test
    void shouldRejectCountingNullPosition() {
        PositionHistory history = new PositionHistory();

        assertThrows(
                NullPointerException.class,
                () -> history.countOccurrences(null)
        );
    }

    @Test
    void shouldClearAllRecordedPositions() {
        PositionHistory history = new PositionHistory();

        PositionKey whiteToMove = createPosition(PieceColor.WHITE);
        PositionKey blackToMove = createPosition(PieceColor.BLACK);

        history.recordPosition(whiteToMove);
        history.recordPosition(whiteToMove);
        history.recordPosition(blackToMove);

        history.clear();

        assertEquals(0, history.countOccurrences(whiteToMove));
        assertEquals(0, history.countOccurrences(blackToMove));
    }

    @Test
    void shouldStartCountingFromOneAfterClearingHistory() {
        PositionHistory history = new PositionHistory();
        PositionKey position = createPosition(PieceColor.WHITE);

        history.recordPosition(position);
        history.recordPosition(position);

        history.clear();
        history.recordPosition(position);

        assertEquals(1, history.countOccurrences(position));
    }

    private PositionKey createPosition(PieceColor currentTurn) {
        String boardLayout =
                "....K..."
                        + "........".repeat(6)
                        + "....k...";

        return new PositionKey(
                boardLayout,
                currentTurn,
                CastlingRights.none(),
                null
        );
    }
}