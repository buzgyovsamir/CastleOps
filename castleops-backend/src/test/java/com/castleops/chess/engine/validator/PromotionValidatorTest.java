package com.castleops.chess.engine.validator;

import com.castleops.chess.model.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PromotionValidatorTest {

    private final PromotionValidator validator = new PromotionValidator();

    @Test
    void shouldAllowWhitePawnPromotionOnLastRank() {
        Piece pawn = new Piece(PieceType.PAWN, PieceColor.WHITE);

        assertTrue(validator.isValidPromotion(
                pawn, move("e7", "e8", PieceType.QUEEN)
        ));
    }

    @Test
    void shouldAllowBlackPawnPromotionOnLastRank() {
        Piece pawn = new Piece(PieceType.PAWN, PieceColor.BLACK);

        assertTrue(validator.isValidPromotion(
                pawn, move("e2", "e1", PieceType.KNIGHT)
        ));
    }

    @Test
    void shouldRequirePromotionForWhitePawnOnLastRank() {
        Piece pawn = new Piece(PieceType.PAWN, PieceColor.WHITE);

        assertFalse(validator.isValidPromotion(
                pawn, move("e7", "e8", null)
        ));
    }

    @Test
    void shouldRequirePromotionForBlackPawnOnLastRank() {
        Piece pawn = new Piece(PieceType.PAWN, PieceColor.BLACK);

        assertFalse(validator.isValidPromotion(
                pawn, move("e2", "e1", null)
        ));
    }

    @Test
    void shouldRejectPromotionBeforeLastRank() {
        Piece pawn = new Piece(PieceType.PAWN, PieceColor.WHITE);

        assertFalse(validator.isValidPromotion(
                pawn, move("e6", "e7", PieceType.QUEEN)
        ));
    }

    @Test
    void shouldAllowRegularPawnMoveWithoutPromotion() {
        Piece pawn = new Piece(PieceType.PAWN, PieceColor.WHITE);

        assertTrue(validator.isValidPromotion(
                pawn, move("e2", "e4", null)
        ));
    }

    @Test
    void shouldRejectPromotionForOtherPieces() {
        for (PieceType type : PieceType.values()) {
            if (type == PieceType.PAWN) {
                continue;
            }

            Piece piece = new Piece(type, PieceColor.WHITE);

            assertFalse(validator.isValidPromotion(
                    piece, move("e7", "e8", PieceType.QUEEN)
            ), "Promotion should be rejected for " + type);
        }
    }

    @Test
    void shouldAllowOtherPiecesWithoutPromotion() {
        Piece rook = new Piece(PieceType.ROOK, PieceColor.WHITE);

        assertTrue(validator.isValidPromotion(
                rook, move("e7", "e8", null)
        ));
    }

    @Test
    void shouldAllowPromotionDuringCapture() {
        Piece pawn = new Piece(PieceType.PAWN, PieceColor.WHITE);

        assertTrue(validator.isValidPromotion(
                pawn, move("e7", "f8", PieceType.BISHOP)
        ));
    }

    private Move move(String from, String to, PieceType promotionType) {
        return new Move(
                Position.fromAlgebraic(from),
                Position.fromAlgebraic(to),
                promotionType
        );
    }
}