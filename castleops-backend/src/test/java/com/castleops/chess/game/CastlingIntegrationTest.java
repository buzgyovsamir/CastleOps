package com.castleops.chess.game;

import com.castleops.chess.engine.generator.LegalMoveGenerator;
import com.castleops.chess.engine.validator.LegalMoveValidator;
import com.castleops.chess.model.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CastlingIntegrationTest {

    private final LegalMoveValidator validator = new LegalMoveValidator();
    private final MoveExecutor executor = new MoveExecutor();

    @Test
    void shouldAllowAndUndoBothSidesForBothColors() {
        for (PieceColor color : PieceColor.values()) {
            for (CastlingSide side : CastlingSide.values()) {
                GameState state = castlingPosition(color);
                Board board = state.getBoard();

                int rank = color == PieceColor.WHITE ? 0 : 7;
                boolean kingSide = side == CastlingSide.KING_SIDE;

                Position kingFrom = new Position(4, rank);
                Position kingTo = new Position(kingSide ? 6 : 2, rank);
                Position rookFrom = new Position(kingSide ? 7 : 0, rank);
                Position rookTo = new Position(kingSide ? 5 : 3, rank);

                Move move = new Move(kingFrom, kingTo);

                assertTrue(validator.isLegalMove(state, move));

                // Пробната проверка трябва да е възстановила позицията.
                assertEquals(CastlingRights.initial(), state.getCastlingRights());
                assertEquals(kingFrom, state.getKingPosition(color));
                assertNotNull(board.getPiece(rookFrom));
                assertNull(board.getPiece(rookTo));

                MoveExecutor.UndoState undo = executor.apply(state, move);

                assertNull(board.getPiece(kingFrom));
                assertNull(board.getPiece(rookFrom));
                assertEquals(
                        new Piece(PieceType.KING, color),
                        board.getPiece(kingTo)
                );
                assertEquals(
                        new Piece(PieceType.ROOK, color),
                        board.getPiece(rookTo)
                );
                assertFalse(state.getCastlingRights().allows(
                        color, CastlingSide.KING_SIDE
                ));
                assertFalse(state.getCastlingRights().allows(
                        color, CastlingSide.QUEEN_SIDE
                ));

                executor.undo(state, undo);

                assertEquals(
                        new Piece(PieceType.KING, color),
                        board.getPiece(kingFrom)
                );
                assertEquals(
                        new Piece(PieceType.ROOK, color),
                        board.getPiece(rookFrom)
                );
                assertNull(board.getPiece(kingTo));
                assertNull(board.getPiece(rookTo));
                assertEquals(kingFrom, state.getKingPosition(color));
                assertEquals(CastlingRights.initial(), state.getCastlingRights());
                assertEquals(color, state.getCurrentTurn());
            }
        }
    }

    @Test
    void shouldRejectCastlingOutOfThroughOrIntoCheck() {
        for (String attackingSquare : new String[]{"e8", "f8", "g8"}) {
            GameState state = castlingPosition(PieceColor.WHITE);

            state.getBoard().setPiece(position("e8"), null);
            state.getBoard().setPiece(
                    position("b8"),
                    new Piece(PieceType.KING, PieceColor.BLACK)
            );
            state.updateKingPosition(PieceColor.BLACK, position("b8"));

            state.getBoard().setPiece(
                    position(attackingSquare),
                    new Piece(PieceType.ROOK, PieceColor.BLACK)
            );

            assertFalse(
                    validator.isLegalMove(state, move("e1", "g1")),
                    "Castling must fail with attacker on " + attackingSquare
            );

            assertEquals(
                    new Piece(PieceType.KING, PieceColor.WHITE),
                    state.getBoard().getPiece(position("e1"))
            );
            assertNull(state.getBoard().getPiece(position("f1")));
            assertNull(state.getBoard().getPiece(position("g1")));
            assertEquals(CastlingRights.initial(), state.getCastlingRights());
        }
    }

    @Test
    void shouldRejectQueensideCastlingWhenBFileIsOccupied() {
        GameState state = castlingPosition(PieceColor.WHITE);

        state.getBoard().setPiece(
                position("b1"),
                new Piece(PieceType.KNIGHT, PieceColor.WHITE)
        );

        assertFalse(validator.isLegalMove(state, move("e1", "c1")));
    }

    @Test
    void shouldAllowQueensideCastlingWhenOnlyBFileIsAttacked() {
        GameState state = castlingPosition(PieceColor.WHITE);

        state.getBoard().setPiece(
                position("b8"),
                new Piece(PieceType.ROOK, PieceColor.BLACK)
        );

        assertTrue(validator.isLegalMove(state, move("e1", "c1")));
    }

    @Test
    void shouldRejectMissingOrWrongRook() {
        Piece[] invalidRooks = {
                null,
                new Piece(PieceType.ROOK, PieceColor.BLACK),
                new Piece(PieceType.BISHOP, PieceColor.WHITE)
        };

        for (Piece piece : invalidRooks) {
            GameState state = castlingPosition(PieceColor.WHITE);
            state.getBoard().setPiece(position("h1"), piece);

            assertFalse(validator.isLegalMove(state, move("e1", "g1")));
        }
    }

    @Test
    void shouldRejectRevokedRightButAllowOtherSide() {
        GameState state = castlingPosition(PieceColor.WHITE);
        state.revokeCastlingRight(
                PieceColor.WHITE, CastlingSide.KING_SIDE
        );

        assertFalse(validator.isLegalMove(state, move("e1", "g1")));
        assertTrue(validator.isLegalMove(state, move("e1", "c1")));
    }

    @Test
    void shouldGenerateBothCastlingMovesWithoutChangingRights() {
        GameState state = castlingPosition(PieceColor.WHITE);
        LegalMoveGenerator generator = new LegalMoveGenerator();

        var moves = generator.generateLegalMoves(state);

        assertTrue(moves.contains(move("e1", "g1")));
        assertTrue(moves.contains(move("e1", "c1")));
        assertEquals(CastlingRights.initial(), state.getCastlingRights());
        assertEquals(position("e1"),
                state.getKingPosition(PieceColor.WHITE));
    }

    @Test
    void shouldExecuteCastlingThroughChessGame() {
        ChessGame game = new ChessGame();

        Move[] setupMoves = {
                move("e2", "e4"),
                move("e7", "e5"),
                move("g1", "f3"),
                move("b8", "c6"),
                move("f1", "e2"),
                move("g8", "f6")
        };

        for (Move move : setupMoves) {
            assertTrue(game.makeMove(move), "Setup move failed: " + move);
        }

        assertTrue(game.makeMove(move("e1", "g1")));

        assertNull(game.getPiece(position("e1")));
        assertNull(game.getPiece(position("h1")));
        assertEquals(
                new Piece(PieceType.KING, PieceColor.WHITE),
                game.getPiece(position("g1"))
        );
        assertEquals(
                new Piece(PieceType.ROOK, PieceColor.WHITE),
                game.getPiece(position("f1"))
        );
        assertEquals(PieceColor.BLACK, game.getCurrentTurn());
    }

    @Test
    void shouldRejectBothCastlingSidesAfterKingReturnsHome() {
        GameState state = castlingPosition(PieceColor.WHITE);

        executor.apply(state, move("e1", "e2"));
        executor.apply(state, move("e2", "e1"));

        assertFalse(validator.isLegalMove(state, move("e1", "g1")));
        assertFalse(validator.isLegalMove(state, move("e1", "c1")));

        assertEquals(
                new CastlingRights(false, false, true, true),
                state.getCastlingRights()
        );
    }
    @Test
    void shouldRejectOnlyKingsideCastlingAfterRookReturnsHome() {
        GameState state = castlingPosition(PieceColor.WHITE);

        executor.apply(state, move("h1", "h2"));
        executor.apply(state, move("h2", "h1"));

        assertFalse(validator.isLegalMove(state, move("e1", "g1")));
        assertTrue(validator.isLegalMove(state, move("e1", "c1")));

        assertEquals(
                new CastlingRights(false, true, true, true),
                state.getCastlingRights()
        );
    }

    @Test
    void shouldRejectOnlyQueensideCastlingAfterRookReturnsHome() {
        GameState state = castlingPosition(PieceColor.WHITE);

        executor.apply(state, move("a1", "a2"));
        executor.apply(state, move("a2", "a1"));

        assertFalse(validator.isLegalMove(state, move("e1", "c1")));
        assertTrue(validator.isLegalMove(state, move("e1", "g1")));

        assertEquals(
                new CastlingRights(true, false, true, true),
                state.getCastlingRights()
        );
    }

    @Test
    void shouldPreserveCastlingRightsAfterValidatingRookMove() {
        GameState state = castlingPosition(PieceColor.WHITE);

        assertTrue(validator.isLegalMove(state, move("h1", "h2")));

        assertEquals(CastlingRights.initial(), state.getCastlingRights());
        assertEquals(
                new Piece(PieceType.ROOK, PieceColor.WHITE),
                state.getBoard().getPiece(position("h1"))
        );
        assertNull(state.getBoard().getPiece(position("h2")));

        assertTrue(validator.isLegalMove(state, move("e1", "g1")));
    }

    @Test
    void shouldPreserveCastlingRightsAfterRejectedKingMove() {
        GameState state = castlingPosition(PieceColor.WHITE);

        state.getBoard().setPiece(
                position("d8"),
                new Piece(PieceType.ROOK, PieceColor.BLACK)
        );

        assertFalse(validator.isLegalMove(state, move("e1", "d1")));

        assertEquals(CastlingRights.initial(), state.getCastlingRights());
        assertEquals(position("e1"),
                state.getKingPosition(PieceColor.WHITE));

        assertEquals(
                new Piece(PieceType.KING, PieceColor.WHITE),
                state.getBoard().getPiece(position("e1"))
        );
        assertNull(state.getBoard().getPiece(position("d1")));

        assertTrue(validator.isLegalMove(state, move("e1", "g1")));
    }

    @Test
    void shouldRestoreEnPassantTargetWhenCastlingIsUndone() {
        GameState state = castlingPosition(PieceColor.WHITE);

        state.getBoard().setPiece(
                position("b5"),
                new Piece(PieceType.PAWN, PieceColor.BLACK)
        );
        state.updateEnPassantTarget(position("b6"));

        Move castling = move("e1", "g1");

        assertTrue(validator.isLegalMove(state, castling));
        assertEquals(position("b6"), state.getEnPassantTarget());

        MoveExecutor.UndoState undoState = executor.apply(state, castling);

        assertNull(state.getEnPassantTarget());

        executor.undo(state, undoState);

        assertEquals(position("b6"), state.getEnPassantTarget());
        assertEquals(CastlingRights.initial(), state.getCastlingRights());
        assertEquals(position("e1"),
                state.getKingPosition(PieceColor.WHITE));

        assertEquals(
                new Piece(PieceType.KING, PieceColor.WHITE),
                state.getBoard().getPiece(position("e1"))
        );
        assertEquals(
                new Piece(PieceType.ROOK, PieceColor.WHITE),
                state.getBoard().getPiece(position("h1"))
        );
        assertNull(state.getBoard().getPiece(position("f1")));
        assertNull(state.getBoard().getPiece(position("g1")));
    }

    private GameState castlingPosition(PieceColor color) {
        GameState state = new GameState();
        Board board = state.getBoard();

        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                board.setPiece(new Position(file, rank), null);
            }
        }

        int homeRank = color == PieceColor.WHITE ? 0 : 7;
        int opponentRank = color == PieceColor.WHITE ? 7 : 0;

        Position kingPosition = new Position(4, homeRank);
        Position opponentKingPosition = new Position(4, opponentRank);

        board.setPiece(kingPosition, new Piece(PieceType.KING, color));
        board.setPiece(
                opponentKingPosition,
                new Piece(PieceType.KING, color.opposite())
        );
        board.setPiece(
                new Position(0, homeRank),
                new Piece(PieceType.ROOK, color)
        );
        board.setPiece(
                new Position(7, homeRank),
                new Piece(PieceType.ROOK, color)
        );

        state.updateKingPosition(color, kingPosition);
        state.updateKingPosition(color.opposite(), opponentKingPosition);

        if (color == PieceColor.BLACK) {
            state.switchTurn();
        }

        return state;
    }

    private Position position(String square) {
        return Position.fromAlgebraic(square);
    }

    private Move move(String from, String to) {
        return new Move(position(from), position(to));
    }
}