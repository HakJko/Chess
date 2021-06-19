package com.epam.ik.entity.pieces.impl;

import com.epam.ik.entity.Position;
import com.epam.ik.entity.pieces.Piece;

import java.util.ArrayList;
import java.util.List;

public class Knight extends Piece {
    public Knight(Colour colour, Position position) {
        super(colour, position);
        pieceName = colour.getName() + "Knight";
    }

    public static List<Position> getKnightAttackPositions(Position position) {
        List<Position> knightPositions = new ArrayList<>();
        int[] xCoords = new int[]{1, 2, 2, 1, -1, -2, -2, -1};
        int[] yCoords = new int[]{2, 1, -1, -2, -2, -1, 1, 2};
        for (int i = 0; i < xCoords.length; i++) {
            Position knightPosition = Position.createPosition(position.getXCoord() + xCoords[i],
                    position.getYCoord() + yCoords[i]);
            if (knightPosition != null) {
                knightPositions.add(knightPosition);
            }
        }
        return knightPositions;
    }

    @Override
    public List<List<Position>> deriveAllMoves() {
        List<List<Position>> listHolder = new ArrayList<>();
        List<Position> knightPositions = Knight.getKnightAttackPositions(position);
        for (Position position : knightPositions) {
            List<Position> moveList = new ArrayList<>();
            moveList.add(position);
            listHolder.add(moveList);
        }
        return listHolder;
    }
}
