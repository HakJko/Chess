package com.epam.ik.entity.pieces;


import com.epam.ik.entity.Position;

import java.util.ArrayList;
import java.util.List;

public abstract class Piece
{
    protected String pieceName;
    protected Colour colour;
    protected Position position;
    private boolean hasMoved = false;

    public Piece(Colour colour, Position position) {
        this.colour = colour;
        this.position = position;
    }

    public static final Piece createChessPiece(String name, Colour colour,
                                               Position position)
    {
        if (name.equals("Pawn")){
            return new Pawn(colour, position);
        }

        if (name.equals("Rook")){
            return new Rook(colour, position);
        }

        if (name.equals("Knight")) {
            return new Knight(colour, position);
        }
        if (name.equals("Bishop")){
            return new Bishop(colour, position);
        }
        if (name.equals("Queen")) {
            return new Queen(colour, position);
        }
        if (name.equals("King")){
            return new King(colour, position);
        }
        return null;
    }

    protected static boolean addMove(List<Position> moveList, Position currentPosition, int xTrans, int yTrans)
    {
        Position subsequentPosition =
                Position.createPosition(xTrans + currentPosition.getXCoord(),
                        yTrans + currentPosition.getYCoord());
        if (subsequentPosition != null) {
            moveList.add(subsequentPosition);
            return true;
        }
        return false;
    }

    /*
     * To be used by Rooks and Queens.
     */
    protected static void addStraightTranslations(List<List<Position>> listHolder, Position currentPosition)
    {
        List<Position> moveListUp = new ArrayList<>();
        for (int yTrans = 1; ; yTrans++) {
            boolean result = addMove(moveListUp, currentPosition, 0, yTrans);
            if (result == false) {
                break;
            }
        }
        if (moveListUp.size() > 0){
            listHolder.add(moveListUp);
        }

        List<Position> moveListDown = new ArrayList<>();
        for (int yTrans = -1; ; yTrans--) {
            boolean result = addMove(moveListDown, currentPosition, 0, yTrans);
            if (result == false)
                break;
        }
        if (moveListDown.size() > 0){
            listHolder.add(moveListDown);
        }

        List<Position> moveListRight = new ArrayList<>();
        for (int xTrans = 1; ; xTrans++) {
            boolean result = addMove(moveListRight, currentPosition, xTrans, 0);
            if (result == false)
                break;
        }
        if (moveListRight.size() > 0){
            listHolder.add(moveListRight);
        }

        List<Position> moveListLeft = new ArrayList<>();
        for (int xTrans = -1; ; xTrans--) {
            boolean result = addMove(moveListLeft, currentPosition, xTrans, 0);
            if (result == false)
                break;
        }
        if (moveListLeft.size() > 0) {
            listHolder.add(moveListLeft);
        }
    }

    /*
     * To be used by Bishops and Queens.
     */
    protected static void addDiagonalTranslations(List<List<Position>> listHolder, Position currentPosition)
    {
        List<Position> moveListNE = new ArrayList<>();
        for (int xTrans = 1, yTrans = 1; ; xTrans++, yTrans++) {
            boolean result = addMove(moveListNE, currentPosition, xTrans, yTrans);
            if (result == false) {
                break;
            }
        }

        if (moveListNE.size() > 0) {
            listHolder.add(moveListNE);
        }

        List<Position> moveListNW = new ArrayList<>();
        for (int xTrans = -1, yTrans = 1; ; xTrans--, yTrans++) {
            boolean result = addMove(moveListNW, currentPosition, xTrans, yTrans);
            if (result == false) {
                break;
            }
        }
        if (moveListNW.size() > 0) {
            listHolder.add(moveListNW);
        }

        List<Position> moveListSE = new ArrayList<>();
        for (int xTrans = 1, yTrans = -1; ; xTrans++, yTrans--) {
            boolean result = addMove(moveListSE, currentPosition, xTrans, yTrans);
            if (result == false) {
                break;
            }
        }
        if (moveListSE.size() > 0) {
            listHolder.add(moveListSE);
        }

        List<Position> moveListSW = new ArrayList<>();
        for (int xTrans = -1, yTrans = -1; ; xTrans--, yTrans--) {
            boolean result = addMove(moveListSW, currentPosition, xTrans, yTrans);
            if (result == false) {
                break;
            }
        }
        if (moveListSW.size() > 0) {
            listHolder.add(moveListSW);
        }
    }

    @Override
    public boolean equals(Object candidate) {
        if (this == candidate)
            return true;

        if (!(candidate instanceof Piece))
            return false;

        Piece confirmed = (Piece) candidate;

        return getPieceName().equals(confirmed.getPieceName())
                && getColour().getName().equals(confirmed.getColour().getName())
                && getPosition().equals(confirmed.getPosition());
    }

    @Override
    public int hashCode() {
        return pieceName.length() * position.getXCoord() + position.getYCoord();
    }

    @Override
    public Piece clone() {
        Piece newClass = null;
        Class classType = getClass();
        if (classType == Bishop.class)
            newClass = new Bishop(getColour(), getPosition());
        else if (classType == King.class)
            newClass = new King(getColour(), getPosition());
        else if (classType == Knight.class)
            newClass = new Knight(getColour(), getPosition());
        else if (classType == Pawn.class)
            newClass = new Pawn(getColour(), getPosition());
        else if (classType == Queen.class)
            newClass = new Queen(getColour(), getPosition());
        else if (classType == Rook.class)
            newClass = new Rook(getColour(), getPosition());
        else
            assert false;
        newClass.hasMoved = hasMoved;
        return newClass;
    }

    @Override
    public String toString() {
        return pieceName + ", " + position + ", " + hasMoved;
    }

    public abstract List<List<Position>> deriveAllMoves();

    public String getName() {
        return pieceName;
    }

    public Position getPosition() {
        return this.position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public Colour getColour() {
        return this.colour;
    }

    public void markAsHavingMoved() {
        hasMoved = true;
    }

    public boolean hasMoved() {
        return hasMoved;
    }

    public String getPieceName() {
        return pieceName;
    }

    public enum Colour {
        WHITE("White"), BLACK("Black");

        private String name;

        Colour(String name) {
            this.name = name;
        }
        public String getName() {
            return name;
        }
    }
}
