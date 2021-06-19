package com.epam.ik.logic;

import com.epam.ik.GameController;
import com.epam.ik.entity.Board;
import com.epam.ik.entity.Position;
import com.epam.ik.entity.pieces.*;
import com.epam.ik.entity.pieces.impl.*;
import com.epam.ik.logic.castling.CastlingOpportunities;

import java.util.ArrayList;
import java.util.List;

public class Checker {
    private final GameController gc;
    private int previousNumberOfChessPieces = 32;
    private int remainingNumberOfMoves = 100;
    private final List<ChessBoardMoment> previousMoments = new ArrayList<>();

    public Checker(GameController gameController) {
        gc = gameController;
        CastlingOpportunities.resetStaticVariables();
    }

    public StalemateOption isStalemate() {
        if (!currentPlayerAbleToMove()) {
            return StalemateOption.MANDATORY_PLAYER_CANT_MOVE;
        }
        if (tooFewPiecesForCheckmate()) {
            return StalemateOption.MANDATORY_TOO_FEW_PIECES;
        }
        if (threeFoldRepetition()) {
            return StalemateOption.OPTIONAL_THREE_FOLD;
        }
        if (fiftyMoveRepetition()) {
            return StalemateOption.OPTIONAL_FIFTY_MOVE;
        }
        return StalemateOption.NOT_STALEMATE;
    }

    public void addChessBoardMoment(ChessBoardMoment chessBoardMoment) {
        previousMoments.add(chessBoardMoment);
    }

    private boolean currentPlayerAbleToMove() {
        Piece.Colour currentPlayerToMove = gc.getCurrentPlayerToMove();
        List<Piece> playersPieces = gc.getChessBoard().getPlayersPieces(currentPlayerToMove);
        for (Piece piece : playersPieces) {
            List<Position> allowedMoves = gc.getAllowedMovesForPiece(piece);
            if (allowedMoves.size() > 0) {
                return true;
            }
        }
        return false;
    }

    private boolean tooFewPiecesForCheckmate() {
        Piece.Colour[] bothPlayers = {Piece.Colour.WHITE, Piece.Colour.BLACK};
        Board chessBoard = gc.getChessBoard();
        for (Piece.Colour player : bothPlayers) {
            Piece.Colour existingBishopSquareColour = null;
            int knightCount = 0;

            List<Piece> playersPieces = chessBoard.getPlayersPieces(player);
            for (Piece piece : playersPieces) {
                if (piece instanceof King) {
                    continue;
                }
                if (piece instanceof Queen || piece instanceof Rook || piece instanceof Pawn) {
                    return false;
                }

                if (piece instanceof Knight) {
                    knightCount++;
                    if (knightCount > 2) {
                        return false;
                    }
                } else {
                    Piece.Colour bishopSquareColour = Board.getColourOfSquareAtPosition(piece.getPosition());
                    if (existingBishopSquareColour != null &&
                            !bishopSquareColour.equals(existingBishopSquareColour)) {
                        return false;
                    }
                    existingBishopSquareColour = bishopSquareColour;
                }
            }
        }
        return true;
    }

    private boolean threeFoldRepetition() {
        if (previousMoments.size() < 9) {
            return false;
        }

        int threeFoldCounter = 1;
        for (int i = previousMoments.size() - 1; i >= 4; i -= 4) {
            if (previousMoments.get(previousMoments.size() - 1).equals(previousMoments.get(i - 4))) {
                threeFoldCounter++;
            }
        }
        return threeFoldCounter >= 3;
    }

    private boolean fiftyMoveRepetition() {
        return remainingNumberOfMoves == 0;
    }

    public int getPreviousNumberOfChessPieces() {
        return previousNumberOfChessPieces;
    }

    public void setPreviousNumberOfChessPieces(int previousNumberOfChessPieces) {
        this.previousNumberOfChessPieces = previousNumberOfChessPieces;
    }

    public void resetToFiftyMoves() {
        remainingNumberOfMoves = 99;
    }

    public void decrementRemainingMoveNumber() {
        remainingNumberOfMoves--;
    }

    public List<ChessBoardMoment> getPreviousMoments() {
        return previousMoments;
    }

    public enum StalemateOption {
        MANDATORY_PLAYER_CANT_MOVE, MANDATORY_TOO_FEW_PIECES,
        OPTIONAL_THREE_FOLD, OPTIONAL_FIFTY_MOVE, NOT_STALEMATE
    }

}
