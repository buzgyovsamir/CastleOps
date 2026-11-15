package com.castleops.chess.engine;

import com.castleops.chess.game.CastlingRights;
import com.castleops.chess.game.PositionHistory;
import com.castleops.chess.model.PieceColor;
import com.castleops.chess.model.PositionKey;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RepetitionDrawRuleTest {

    private final RepetitionDrawRule rule = new RepetitionDrawRule();

    @ParameterizedTest
    @CsvSource({
            "0, false, false",
            "1, false, false",
            "2, false, false",
            "3, true,  false",
            "4, true,  false",
            "5, true,  true",
            "6, true,  true"
    })
    void shouldEvaluateRepetitionThresholds(
            int occurrenceCount,
            boolean claimExpected,
            boolean automaticDrawExpected
    ) {
        PositionHistory history = new PositionHistory();
        PositionKey position = new PositionKey(
                "....K..." + "........".repeat(6) + "....k...",
                PieceColor.WHITE,
                CastlingRights.none(),
                null
        );

        for (int occurrence = 0; occurrence < occurrenceCount; occurrence++) {
            history.recordPosition(position);
        }

        assertEquals(
                claimExpected,
                rule.hasReachedClaimThreshold(history, position)
        );

        assertEquals(
                automaticDrawExpected,
                rule.hasReachedAutomaticDrawThreshold(history, position)
        );
    }
}