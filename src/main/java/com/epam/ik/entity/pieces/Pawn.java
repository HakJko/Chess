package com.epam.ik.entity.pieces;

import com.epam.ik.entity.Position;

import java.util.ArrayList;
import java.util.List;

public class Pawn extends Piece
{
    public Pawn(Colour colour, Position position) {
        super(colour, position);
        pieceName = colour.getName() + "Pawn";
    }

    @Override
    public List<List<Position>> deriveAllMoves() {
        List<List<Position>> listHolder = new ArrayList<>();
        List<Position> moveList1 = new ArrayList<>();
        Piece.addMove(moveList1, position, 0,
                (colour == Colour.WHITE) ? 1 : -1);

        if (position.getYCoord() == 2 && colour == Colour.WHITE) {
            Piece.addMove(moveList1, position, 0, 2);
        }
        else if (position.getYCoord() == 7 && colour == Colour.BLACK) {
            Piece.addMove(moveList1, position, 0, -2);
        }

        if (moveList1.size() > 0) {
            listHolder.add(moveList1);
        }

        List<Position> moveList2 = new ArrayList<>();
        Piece.addMove(moveList2, position, 1,
                (colour == Colour.WHITE) ? 1 : -1);
        if (moveList2.size() > 0) {
            listHolder.add(moveList2);
        }

        List<Position> moveList3 = new ArrayList<>();
        Piece.addMove(moveList3, position, -1,
                (colour == Colour.WHITE) ? 1 : -1);
        if (moveList3.size() > 0) {
            listHolder.add(moveList3);
        }

        return listHolder;
    }

    public boolean adjacentToEnPassantPosition(Position enPassantPosition) {
        return (position.getYCoord() == enPassantPosition.getYCoord())
                && (Math.abs(position.getXCoord() - enPassantPosition.getXCoord()) == 1);
    }

    public Position finalPositionAfterEnPassant(Position enPassantPosition) {
        int xCoord = enPassantPosition.getXCoord();
        int yCoord = enPassantPosition.getYCoord() + ((colour == Colour.WHITE) ? 1 : -1);
        return Position.createPosition(xCoord, yCoord);
    }
}
