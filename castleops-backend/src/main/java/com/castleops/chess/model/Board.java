
package com.castleops.chess.model;

import java.util.Objects;

public class Board {

    private final Piece[][] squares;

    public Board() {
        this.squares = new Piece[8][8];
    }

    public Piece getPiece(Position position) {
        Objects.requireNonNull(position, "Position cannot be null");

        return squares[position.rank()][position.file()];
    }

    public void setPiece(Position position, Piece piece) {
        Objects.requireNonNull(position, "Position cannot be null");

        squares[position.rank()][position.file()] = piece;
    }

    public boolean isEmpty(Position position) {
        return getPiece(position) == null;
    }


    public void setupInitialPosition() {
        // Clear the board
        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                squares[rank][file] = null;
            }
        }

        PieceType[] backRank = {
                PieceType.ROOK,
                PieceType.KNIGHT,
                PieceType.BISHOP,
                PieceType.QUEEN,
                PieceType.KING,
                PieceType.BISHOP,
                PieceType.KNIGHT,
                PieceType.ROOK
        };

        for (int file = 0; file < 8; file++) {
            // White pieces
            squares[0][file] = new Piece(
                    backRank[file], PieceColor.WHITE
            );
            squares[1][file] = new Piece(
                    PieceType.PAWN, PieceColor.WHITE
            );

            // Black pieces
            squares[7][file] = new Piece(
                    backRank[file], PieceColor.BLACK
            );
            squares[6][file] = new Piece(
                    PieceType.PAWN, PieceColor.BLACK
            );
        }
    }

}
