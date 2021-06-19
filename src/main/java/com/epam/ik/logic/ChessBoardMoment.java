package com.epam.ik.logic;

import com.epam.ik.entity.Position;
import com.epam.ik.entity.pieces.Piece;
import com.epam.ik.logic.castling.CastlingOpportunities;
import com.epam.ik.logic.castling.CastlingPiecesMovementTracker;

import java.util.Map;

public class ChessBoardMoment {
    private final Map<Position, Piece> chessPieces;
    private final CastlingOpportunities castlingOpportunities;
    private final CastlingPiecesMovementTracker castlingPiecesMovementTracker;
    private final GameControllerStateInfo gcState;

    public ChessBoardMoment(Map<Position, Piece> chessPieces,
                            CastlingOpportunities castlingOpportunities,
                            CastlingPiecesMovementTracker castlingPiecesMovementTracker,
                            GameControllerStateInfo gcState) {
        this.chessPieces = chessPieces;
        this.castlingOpportunities = castlingOpportunities;
        this.castlingPiecesMovementTracker = castlingPiecesMovementTracker;
        this.gcState = gcState;
    }

    @Override
    public boolean equals(Object candidate) {
        if (this == candidate) {
            return true;
        }

        if (!(candidate instanceof ChessBoardMoment)) {
            return false;
        }

        ChessBoardMoment confirmed = (ChessBoardMoment) candidate;
        boolean sameCastlingOpportunities = getCastlingOpportunities().equals(confirmed.getCastlingOpportunities());
        boolean sameCastlingPiecesMovementTracker = getCastlingPiecesMovementTracker().equals(confirmed.getCastlingPiecesMovementTracker());
        boolean sameEnPassantPosition = getEnPassantPosition() != null && getEnPassantPosition().equals(confirmed.getEnPassantPosition())
                || getEnPassantPosition() == null && confirmed.getEnPassantPosition() == null;
        boolean samePiecePositions = getChessPieces().equals(confirmed.getChessPieces());

        return sameCastlingOpportunities && sameCastlingPiecesMovementTracker
                && sameEnPassantPosition && samePiecePositions;
    }

    @Override
    public int hashCode() {
        return 1;
    }

    public Map<Position, Piece> getChessPieces() {
        return chessPieces;
    }

    public CastlingOpportunities getCastlingOpportunities() {
        return castlingOpportunities;
    }

    public CastlingPiecesMovementTracker getCastlingPiecesMovementTracker() {
        return castlingPiecesMovementTracker;
    }

    public Position getEnPassantPosition() {
        return gcState.getEnPassantPosition();
    }

    public GameControllerStateInfo getGCState() {
        return gcState;
    }
}
