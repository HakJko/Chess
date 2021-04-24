package com.epam.ik.entity.pieces;

import com.epam.ik.entity.Position;

import java.util.ArrayList;
import java.util.List;

public class Bishop extends Piece
{
    public Bishop(Colour colour, Position position) {
        super(colour, position);
        pieceName = colour.getName() + "Bishop";
    }

    @Override
    public List<List<Position>> deriveAllMoves() {
        List<List<Position>> listHolder = new ArrayList<>();
        Piece.addDiagonalTranslations(listHolder, position);
        return listHolder;
    }
}
