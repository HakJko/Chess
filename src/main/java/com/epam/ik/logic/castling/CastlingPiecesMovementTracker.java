package com.epam.ik.logic.castling;

public class CastlingPiecesMovementTracker
{
    private boolean whiteLeftCastleHasMoved;
    private boolean whiteKingHasMoved;
    private boolean whiteRightCastleHasMoved;
    private boolean blackLeftCastleHasMoved;
    private boolean blackKingHasMoved;
    private boolean blackRightKingHasMoved;

    public CastlingPiecesMovementTracker(boolean[] inputs) {
        this.whiteLeftCastleHasMoved = inputs[0];
        this.whiteKingHasMoved = inputs[1];
        this.whiteRightCastleHasMoved = inputs[2];
        this.blackLeftCastleHasMoved = inputs[3];
        this.blackKingHasMoved = inputs[4];
        this.blackRightKingHasMoved = inputs[5];
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof CastlingPiecesMovementTracker)) {
            return false;
        }

        CastlingPiecesMovementTracker candidate = (CastlingPiecesMovementTracker) obj;

        return whiteLeftCastleHasMoved == candidate.getWhiteLeftCastleHasMoved()
                && whiteKingHasMoved == candidate.getWhiteKingHasMoved()
                && whiteRightCastleHasMoved == candidate.getWhiteRightCastleHasMoved()
                && blackLeftCastleHasMoved == candidate.getBlackLeftCastleHasMoved()
                && blackKingHasMoved == candidate.getBlackKingHasMoved()
                && blackRightKingHasMoved == candidate.getBlackRightKingHasMoved();
    }

    @Override
    public int hashCode() {
        return 1;
    }

    public boolean getWhiteLeftCastleHasMoved() {
        return whiteLeftCastleHasMoved;
    }

    public boolean getWhiteKingHasMoved() {
        return whiteKingHasMoved;
    }

    public boolean getWhiteRightCastleHasMoved() {
        return whiteRightCastleHasMoved;
    }

    public boolean getBlackLeftCastleHasMoved() {
        return blackLeftCastleHasMoved;
    }

    public boolean getBlackKingHasMoved() {
        return blackKingHasMoved;
    }

    public boolean getBlackRightKingHasMoved() {
        return blackRightKingHasMoved;
    }

}
