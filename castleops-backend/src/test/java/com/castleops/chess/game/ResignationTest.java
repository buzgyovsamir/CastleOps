package com.castleops.chess.game;

import com.castleops.chess.model.*;
import com.castleops.chess.engine.InsufficientMaterialDetector;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

class ResignationTest {

    @ParameterizedTest
    @EnumSource(PieceColor.class)
    void shouldAllowEitherPlayerToResign(PieceColor player) {
        ChessGame game = new ChessGame();

        assertTrue(game.resign(player));

        assertEquals(GameStatus.RESIGNED, game.getStatus());
        assertEquals(
                GameResult.resignation(player.opposite()),
                game.getResult().orElseThrow()
        );

        assertEquals(PieceColor.WHITE, game.getCurrentTurn());
        assertFalse(game.makeMove(move("e2", "e4")));
        assertTrue(game.getLegalMoves().isEmpty());
    }

    @Test
    void shouldClearPendingDrawOffer() {
        ChessGame game = new ChessGame();

        assertTrue(game.offerDraw(PieceColor.WHITE));
        assertTrue(game.resign(PieceColor.BLACK));

        assertTrue(game.getDrawOfferBy().isEmpty());
        assertFalse(game.acceptDraw(PieceColor.BLACK));
    }

    @Test
    void shouldRejectRepeatedResignationAndPreserveResult() {
        ChessGame game = new ChessGame();

        assertTrue(game.resign(PieceColor.WHITE));
        assertFalse(game.resign(PieceColor.BLACK));

        assertEquals(
                GameResult.resignation(PieceColor.BLACK),
                game.getResult().orElseThrow()
        );
    }

    @Test
    void shouldRejectResignationAfterAgreedDraw() {
        ChessGame game = new ChessGame();

        assertTrue(game.offerDraw(PieceColor.WHITE));
        assertTrue(game.acceptDraw(PieceColor.BLACK));

        assertFalse(game.resign(PieceColor.WHITE));

        assertEquals(
                GameResult.draw(GameEndReason.DRAW_AGREEMENT),
                game.getResult().orElseThrow()
        );
    }

    @Test
    void shouldDrawWhenOpponentHasOnlyKing() {
        GameState state = createKingsOnlyState();
        place(state, "d1", PieceType.QUEEN, PieceColor.WHITE);

        ChessGame game = new ChessGame(state);

        assertEquals(GameStatus.ACTIVE, game.getStatus());
        assertTrue(game.resign(PieceColor.WHITE));

        assertEquals(
                GameResult.draw(
                        GameEndReason.RESIGNATION_WITHOUT_MATING_MATERIAL
                ),
                game.getResult().orElseThrow()
        );
        assertEquals(GameStatus.DRAW, game.getStatus());
    }

    @Test
    void shouldDrawWhenOpponentHasOnlyKnightAgainstQueen() {
        GameState state = createKingsOnlyState();

        place(state, "d1", PieceType.QUEEN, PieceColor.WHITE);
        place(state, "b8", PieceType.KNIGHT, PieceColor.BLACK);

        ChessGame game = new ChessGame(state);

        assertEquals(GameStatus.ACTIVE, game.getStatus());
        assertTrue(game.resign(PieceColor.WHITE));

        assertEquals(
                GameResult.draw(
                        GameEndReason.RESIGNATION_WITHOUT_MATING_MATERIAL
                ),
                game.getResult().orElseThrow()
        );
    }

    @Test
    void shouldAwardWinToKnightSideWhenOpponentHasPawn() {
        GameState state = createKingsOnlyState();

        place(state, "a2", PieceType.PAWN, PieceColor.WHITE);
        place(state, "b8", PieceType.KNIGHT, PieceColor.BLACK);

        ChessGame game = new ChessGame(state);

        assertTrue(game.resign(PieceColor.WHITE));

        assertEquals(
                GameResult.resignation(PieceColor.BLACK),
                game.getResult().orElseThrow()
        );
    }

    @Test
    void shouldRejectNullPlayerWithoutFinishingGame() {
        ChessGame game = new ChessGame();

        assertThrows(
                NullPointerException.class,
                () -> game.resign(null)
        );

        assertEquals(GameStatus.ACTIVE, game.getStatus());
        assertTrue(game.getResult().isEmpty());
    }

    @ParameterizedTest(name = "Bishop side {0}, opponent {1}, cannot mate: {2}")
    @MethodSource("bishopMaterialCases")
    void shouldHandleBishopExceptionsForBothColors(
            PieceColor beneficiary, PieceType opponent, boolean cannotMate) {
        GameState state = createKingsOnlyState();
        place(state, "c1", PieceType.BISHOP, beneficiary);
        // b3 has the opposite square color to c1. Neither rook nor queen
        // on b3 checks either king on e1/e8. A pawn can also promote.
        place(state, "b3", opponent, beneficiary.opposite());
        ChessGame game = new ChessGame(state);
        assertEquals(GameStatus.ACTIVE, game.getStatus());
        assertTrue(game.resign(beneficiary.opposite()));
        assertEquals(cannotMate
                ? GameResult.draw(GameEndReason.RESIGNATION_WITHOUT_MATING_MATERIAL)
                : GameResult.resignation(beneficiary), game.getResult().orElseThrow());
    }

    private static Stream<Arguments> bishopMaterialCases() {
        return Stream.of(PieceColor.values()).flatMap(color -> Stream.of(
                Arguments.of(color, PieceType.ROOK, true),
                Arguments.of(color, PieceType.QUEEN, true),
                Arguments.of(color, PieceType.BISHOP, false),
                Arguments.of(color, PieceType.KNIGHT, false),
                Arguments.of(color, PieceType.PAWN, false)
        ));
    }
    @ParameterizedTest
    @EnumSource(PieceColor.class)
    void shouldRecognizeSameSquareColorBishopsForEitherBeneficiary(PieceColor beneficiary) {
        GameState state = createKingsOnlyState();
        place(state, "c1", PieceType.BISHOP, beneficiary);
        place(state, "e3", PieceType.BISHOP, beneficiary.opposite());
        assertTrue(new InsufficientMaterialDetector()
                .hasInsufficientMaterial(state.getBoard(), beneficiary));
        ChessGame game = new ChessGame(state);
        assertEquals(GameResult.draw(GameEndReason.INSUFFICIENT_MATERIAL), game.getResult().orElseThrow());
        assertFalse(game.resign(beneficiary.opposite()));
    }

    private GameState createKingsOnlyState() {
        GameState state = new GameState();

        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                state.getBoard().setPiece(
                        new Position(file, rank),
                        null
                );
            }
        }

        place(state, "e1", PieceType.KING, PieceColor.WHITE);
        place(state, "e8", PieceType.KING, PieceColor.BLACK);

        state.updateKingPosition(PieceColor.WHITE, position("e1"));
        state.updateKingPosition(PieceColor.BLACK, position("e8"));
        state.restoreCastlingRights(CastlingRights.none());

        return state;
    }

    private void place(
            GameState state,
            String square,
            PieceType type,
            PieceColor color
    ) {
        state.getBoard().setPiece(
                position(square),
                new Piece(type, color)
        );
    }

    private Move move(String from, String to) {
        return new Move(position(from), position(to));
    }

    private Position position(String square) {
        return Position.fromAlgebraic(square);
    }
}
