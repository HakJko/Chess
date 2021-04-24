package com.epam.ik.logic;

import com.epam.ik.entity.Position;
import com.epam.ik.entity.pieces.Piece;

import java.util.ArrayList;
import java.util.List;

import static com.epam.ik.entity.pieces.Piece.Colour;

public class GameControllerStateInfo
{
    public Colour currentPlayerToMove;
    public boolean currentPlayerIsInCheck;
    public Position enPassantPosition;
    public int moveNumber;
    public List<Position> checkBlockingMoves;

    public GameControllerStateInfo() {
        currentPlayerToMove = Piece.Colour.WHITE;
        moveNumber = 0;
    }

    public GameControllerStateInfo(Piece.Colour currentPlayerToMove, List<Position> checkBlockingMoves,
                                   boolean currentPlayerIsInCheck, Position enPassantPosition,
                                   int moveNumber) {
        this.currentPlayerToMove = currentPlayerToMove;
        this.checkBlockingMoves = checkBlockingMoves;
        this.currentPlayerIsInCheck = currentPlayerIsInCheck;
        this.enPassantPosition = enPassantPosition;
        this.moveNumber = moveNumber;
    }

    @Override
    public GameControllerStateInfo clone() throws CloneNotSupportedException {
        List<Position> newCheckBlockingMoves = null;
        if (checkBlockingMoves != null) {
            newCheckBlockingMoves = new ArrayList<>();
            for (Position p : checkBlockingMoves)
                newCheckBlockingMoves.add(p);
        }
        return new GameControllerStateInfo(currentPlayerToMove, newCheckBlockingMoves, currentPlayerIsInCheck, enPassantPosition, moveNumber);
    }

    public Piece.Colour getCurrentPlayerToMove() {
        return currentPlayerToMove;
    }

    public boolean isCurrentPlayerIsInCheck() {
        return currentPlayerIsInCheck;
    }

    public Position getEnPassantPosition() {
        return enPassantPosition;
    }

    public int getMoveNumber() {
        return moveNumber;
    }

    public List<Position> getCheckBlockingMoves() {
        return checkBlockingMoves;
    }

}
