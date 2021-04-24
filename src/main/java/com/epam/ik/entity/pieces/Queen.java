package com.epam.ik.entity.pieces;

import com.epam.ik.entity.Position;

import java.util.ArrayList;
import java.util.List;

public class Queen extends Piece
{
    public Queen(Colour colour, Position position) {
        super(colour, position);
        pieceName = colour.getName() + "Queen";
    }

    @Override
    public List<List<Position>> deriveAllMoves() {
        List<List<Position>> listHolder = new ArrayList<>();
        Piece.addStraightTranslations(listHolder, position);
        Piece.addDiagonalTranslations(listHolder, position);
        return listHolder;
    }
}
