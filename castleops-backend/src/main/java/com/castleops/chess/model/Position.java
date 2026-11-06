package com.castleops.chess.model;

public record Position(int file, int rank) {

    public Position {
        if (file < 0 || file > 7 || rank < 0 || rank > 7) {
            throw new IllegalArgumentException(
                    "Position coordinates must be between 0 and 7"
            );
        }
    }

    public static Position fromAlgebraic(String notation) {
        if (notation == null || !notation.matches("[a-h][1-8]")) {
            throw new IllegalArgumentException(
                    "Invalid chess position: " + notation
            );
        }

        int file = notation.charAt(0) - 'a';
        int rank = notation.charAt(1) - '1';

        return new Position(file, rank);
    }

    public String toAlgebraic() {
        char fileChar = (char) ('a' + file);
        char rankChar = (char) ('1' + rank);

        return "" + fileChar + rankChar;
    }
}
