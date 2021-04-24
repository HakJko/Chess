package com.epam.ik.entity.pieces;

import com.epam.ik.entity.Position;

import java.util.ArrayList;
import java.util.List;

public class Rook extends Piece
{
    public Rook(Colour colour, Position position) {
        super(colour, position);
        pieceName = colour.getName() + "Rook";
    }

    @Override
    public List<List<Position>> deriveAllMoves() {
        List<List<Position>> listHolder = new ArrayList<>();
        Piece.addStraightTranslations(listHolder, position);
        return listHolder;
    }
}
