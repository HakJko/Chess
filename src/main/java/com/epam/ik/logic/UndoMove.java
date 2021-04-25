package com.epam.ik.logic;

import com.epam.ik.GameController;
import com.epam.ik.entity.Board;
import com.epam.ik.entity.Position;
import com.epam.ik.entity.pieces.Piece;

import java.util.*;

public class UndoMove
{
    private final GameController GC;
    private final Board CHESS_BOARD;
    private final List<ChessBoardMoment> PREVIOUS_MOMENTS;
    private int moveNumber;
    private int highestMoveNumber;

    enum Change {
        UNDO
    }

    public UndoMove(GameController gameController, Board CHESS_BOARD,
                    List<ChessBoardMoment> PREVIOUS_MOMENTS) {
        this.GC = gameController;
        this.CHESS_BOARD = CHESS_BOARD;
        this.PREVIOUS_MOMENTS = PREVIOUS_MOMENTS;
    }

    public void undo() {
        changeBoard(Change.UNDO);
    }

    private void changeBoard(Change change) {
        for (ChessBoardMoment c : PREVIOUS_MOMENTS) {
            Set<Position> blah = c.getChessPieces().keySet();
            Set<Position> sortedSet = new TreeSet<>();
            for (Position p : blah) {
                sortedSet.add(p);
            }
        }
        ChessBoardMoment desiredChessBoardMoment = null;
        if (change == Change.UNDO) {
            desiredChessBoardMoment = getRequiredMomentForUndo();
        }
        setGameControllerStateInfo(desiredChessBoardMoment);
        setChessPieces(desiredChessBoardMoment);
        updateVisualBoard(getCurrentMoment().getChessPieces(), desiredChessBoardMoment.getChessPieces());
        CHESS_BOARD.toggleCheckLabel(desiredChessBoardMoment.getGCState().isCurrentPlayerIsInCheck());

        if (GC.getPieceCurrentlyHeld() != null) {
            GC.nullifyPieceAndPossibleMoves();
            CHESS_BOARD.resetAllBoardSquareColours();
        }
        if (change == Change.UNDO)
            moveNumber--;
    }

    private void updateVisualBoard(
            Map<Position, Piece> currentPieces,
            Map<Position, Piece> intendedPieces)
    {
        Piece pieceToAdd;
        Piece pieceToDelete;

        Set<Position> currentPositions = currentPieces.keySet();
        Set<Position> intendedPositions = intendedPieces.keySet();

        List<Piece> piecesToDelete = new ArrayList<>();
        List<Piece> piecesToAdd = new ArrayList<>();

        for (Position intendedPosition : intendedPositions) {
            if (!currentPositions.contains(intendedPosition)
                    || !intendedPieces.get(intendedPosition).equals(currentPieces.get(intendedPosition))) {
                pieceToAdd = intendedPieces.get(intendedPosition);
                if (!pieceToAdd.getPosition().equals(intendedPosition)) {
                    assert false;
                }
                piecesToAdd.add(pieceToAdd);
            }
        }
        for (Position currentPosition : currentPositions) {
            if (!intendedPositions.contains(currentPosition)
                    || !currentPieces.get(currentPosition).equals(intendedPieces.get(currentPosition))) {
                pieceToDelete = currentPieces.get(currentPosition);
                assert pieceToDelete.getPosition().equals(currentPosition);
                piecesToDelete.add(pieceToDelete);
            }
        }

        for (Piece piece : piecesToDelete)
            CHESS_BOARD.removePiece(piece.getPosition());
        for (Piece piece : piecesToAdd)
            CHESS_BOARD.addPiece(piece);
    }

    private void trimPreviousMoments() {
        while (PREVIOUS_MOMENTS.size() > (highestMoveNumber)) {
            PREVIOUS_MOMENTS.remove(PREVIOUS_MOMENTS.size() - 1);
        }
    }

    private ChessBoardMoment getCurrentMoment() {
        return PREVIOUS_MOMENTS.get(moveNumber);
    }

    private ChessBoardMoment getRequiredMomentForUndo() {
        ChessBoardMoment retMoment = PREVIOUS_MOMENTS.get(moveNumber - 1);
        return retMoment;
    }

    public int getHighestMoveNumber() {
        return highestMoveNumber;
    }

    private void setChessPieces(ChessBoardMoment chessBoardMoment) {
        Map<Position, Piece> chessPieces = chessBoardMoment.getChessPieces();
        CHESS_BOARD.setChessPieces(chessPieces);
    }

    public void setHighestMoveNumber(int newMoveNumber)
    {
        highestMoveNumber = newMoveNumber;
        moveNumber = newMoveNumber;
        trimPreviousMoments();
    }

    private void setGameControllerStateInfo(ChessBoardMoment chessBoardMoment) {
        GameControllerStateInfo gcState = chessBoardMoment.getGCState();
        GC.setGcState(gcState);
    }

}
