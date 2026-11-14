package com.castleops.chess.game;

import com.castleops.chess.model.CastlingSide;
import com.castleops.chess.model.PieceColor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CastlingRightsTest {

    @Test
    void shouldInitiallyAllowBothSidesForBothColors() {
        CastlingRights rights = CastlingRights.initial();

        for (PieceColor color : PieceColor.values()) {
            for (CastlingSide side : CastlingSide.values()) {
                assertTrue(rights.allows(color, side));
            }
        }
    }

    @Test
    void shouldAllowNoCastlingWhenRightsAreAbsent() {
        CastlingRights rights = CastlingRights.none();

        for (PieceColor color : PieceColor.values()) {
            for (CastlingSide side : CastlingSide.values()) {
                assertFalse(rights.allows(color, side));
            }
        }
    }

    @Test
    void shouldRemoveOnlyTheSelectedRight() {
        for (PieceColor removedColor : PieceColor.values()) {
            for (CastlingSide removedSide : CastlingSide.values()) {
                CastlingRights updated = CastlingRights.initial()
                        .without(removedColor, removedSide);

                for (PieceColor color : PieceColor.values()) {
                    for (CastlingSide side : CastlingSide.values()) {
                        boolean isRemovedRight =
                                color == removedColor && side == removedSide;

                        assertEquals(
                                !isRemovedRight,
                                updated.allows(color, side),
                                "Unexpected right for " + color + " " + side
                        );
                    }
                }
            }
        }
    }

    @Test
    void shouldRemoveBothSidesOnlyForSelectedColor() {
        for (PieceColor color : PieceColor.values()) {
            CastlingRights updated = CastlingRights.initial()
                    .withoutBothSides(color);

            assertFalse(updated.allows(color, CastlingSide.KING_SIDE));
            assertFalse(updated.allows(color, CastlingSide.QUEEN_SIDE));

            assertTrue(updated.allows(
                    color.opposite(), CastlingSide.KING_SIDE
            ));
            assertTrue(updated.allows(
                    color.opposite(), CastlingSide.QUEEN_SIDE
            ));
        }
    }

    @Test
    void shouldNotModifyOriginalRights() {
        CastlingRights original = CastlingRights.initial();

        CastlingRights updated = original.without(
                PieceColor.WHITE,
                CastlingSide.KING_SIDE
        );

        assertTrue(original.allows(
                PieceColor.WHITE, CastlingSide.KING_SIDE
        ));
        assertFalse(updated.allows(
                PieceColor.WHITE, CastlingSide.KING_SIDE
        ));
    }

    @Test
    void shouldPreservePreviouslyRevokedRights() {
        CastlingRights rights = CastlingRights.initial()
                .without(PieceColor.WHITE, CastlingSide.KING_SIDE)
                .without(PieceColor.BLACK, CastlingSide.QUEEN_SIDE)
                .without(PieceColor.WHITE, CastlingSide.KING_SIDE);

        assertFalse(rights.allows(
                PieceColor.WHITE, CastlingSide.KING_SIDE
        ));
        assertTrue(rights.allows(
                PieceColor.WHITE, CastlingSide.QUEEN_SIDE
        ));
        assertTrue(rights.allows(
                PieceColor.BLACK, CastlingSide.KING_SIDE
        ));
        assertFalse(rights.allows(
                PieceColor.BLACK, CastlingSide.QUEEN_SIDE
        ));
    }

    @Test
    void shouldUpdateGameStateWithoutChangingPreviousSnapshot() {
        GameState state = new GameState();
        CastlingRights previousRights = state.getCastlingRights();

        state.revokeCastlingRight(
                PieceColor.WHITE,
                CastlingSide.KING_SIDE
        );
        state.revokeCastlingRights(PieceColor.BLACK);

        assertEquals(CastlingRights.initial(), previousRights);
        assertEquals(
                new CastlingRights(false, true, false, false),
                state.getCastlingRights()
        );
    }
}