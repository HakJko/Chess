package com.epam.ik.logic.castling;

public class CastlingOpportunities {
    public static Boolean nextWhiteKingCanCastleLeft = false;
    public static Boolean nextWhiteKingCanCastleRight = false;
    public static Boolean nextBlackKingCanCastleLeft = false;
    public static Boolean nextBlackKingCanCastleRight = false;
    private final boolean whiteKingCanCastleLeft;
    private final boolean whiteKingCanCastleRight;
    private final boolean blackKingCanCastleLeft;
    private final boolean blackKingCanCastleRight;

    public CastlingOpportunities(Boolean whiteKingCanCastleLeft,
                                 Boolean whiteKingCanCastleRight,
                                 Boolean blackKingCanCastleLeft,
                                 Boolean blackKingCanCastleRight) {
        if (whiteKingCanCastleLeft == null) {
            this.whiteKingCanCastleLeft = Boolean.TRUE.equals(nextWhiteKingCanCastleLeft);
            nextWhiteKingCanCastleLeft = null;
        } else {
            nextWhiteKingCanCastleLeft = this.whiteKingCanCastleLeft = whiteKingCanCastleLeft;
        }
        if (whiteKingCanCastleRight == null) {
            this.whiteKingCanCastleRight = Boolean.TRUE.equals(nextWhiteKingCanCastleRight);
            nextWhiteKingCanCastleRight = null;
        } else {
            nextWhiteKingCanCastleRight = this.whiteKingCanCastleRight = whiteKingCanCastleRight;
        }
        if (blackKingCanCastleLeft == null) {
            this.blackKingCanCastleLeft = Boolean.TRUE.equals(nextBlackKingCanCastleLeft);
            nextBlackKingCanCastleLeft = null;
        } else {
            nextBlackKingCanCastleLeft = this.blackKingCanCastleLeft = blackKingCanCastleLeft;
        }
        if (blackKingCanCastleRight == null) {
            this.blackKingCanCastleRight = Boolean.TRUE.equals(nextBlackKingCanCastleRight);
            nextBlackKingCanCastleRight = null;
        } else {
            nextBlackKingCanCastleRight = this.blackKingCanCastleRight = blackKingCanCastleRight;
        }
    }

    public static void resetStaticVariables() {
        nextWhiteKingCanCastleLeft = false;
        nextWhiteKingCanCastleRight = false;
        nextBlackKingCanCastleLeft = false;
        nextBlackKingCanCastleRight = false;
    }

    @Override
    public boolean equals(Object candidate) {
        if (this == candidate) {
            return true;
        }

        if (!(candidate instanceof CastlingOpportunities)) {
            return false;
        }

        CastlingOpportunities confirmed = (CastlingOpportunities) candidate;
        return whiteKingCanCastleLeft == confirmed.isWhiteKingCanCastleLeft()
                && whiteKingCanCastleRight == confirmed.isWhiteKingCanCastleRight()
                && blackKingCanCastleLeft == confirmed.isBlackKingCanCastleLeft()
                && blackKingCanCastleRight == confirmed.isBlackKingCanCastleRight();
    }

    @Override
    public int hashCode() {
        return 1;
    }

    @Override
    public String toString() {
        return whiteKingCanCastleLeft + " " + whiteKingCanCastleRight + " " + blackKingCanCastleLeft + " " + blackKingCanCastleRight;
    }

    public boolean isWhiteKingCanCastleLeft() {
        return whiteKingCanCastleLeft;
    }

    public boolean isWhiteKingCanCastleRight() {
        return whiteKingCanCastleRight;
    }

    public boolean isBlackKingCanCastleLeft() {
        return blackKingCanCastleLeft;
    }

    public boolean isBlackKingCanCastleRight() {
        return blackKingCanCastleRight;
    }


}
