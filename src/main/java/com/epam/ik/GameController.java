package com.epam.ik;

import com.epam.ik.entity.Board;
import com.epam.ik.entity.Position;
import com.epam.ik.entity.pieces.*;
import com.epam.ik.entity.pieces.impl.*;
import com.epam.ik.logic.*;
import com.epam.ik.logic.castling.CastlingOpportunities;
import com.epam.ik.logic.castling.CastlingPiecesMovementTracker;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;

import static com.epam.ik.entity.pieces.Piece.Colour;
import static com.epam.ik.logic.Checker.StalemateOption;

public final class GameController {

    private GameControllerStateInfo gcState = new GameControllerStateInfo();
    private final Board chessBoard;
    private Checker stalemateChecker;
    private UndoMove undoMove;
    private EndGame endGame;
    private Piece pieceCurrentlyHeld;
    private List<Position> possibleMoves;

    public GameController() {
        chessBoard = new Board(this);
        stalemateChecker = new Checker(this);
        undoMove = new UndoMove(this, getChessBoard(), stalemateChecker.getPreviousMoments());
        endGame = new EndGame(chessBoard);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            GameController gc = new GameController();

            gc.go();
        });
    }

    private void go() {
        chessBoard.initialiseBoard();
    }

    public void reset() {
        pieceCurrentlyHeld = null;
        possibleMoves = null;
        stalemateChecker = new Checker(this);
        endGame = new EndGame(chessBoard);
        gcState = new GameControllerStateInfo();
        undoMove = new UndoMove(this, getChessBoard(), stalemateChecker.getPreviousMoments());
    }

    public void squareClicked(Position clickedPosition) throws CloneNotSupportedException {

        if (endGame.isGameOver()) {
            return;
        }

        Piece clickedPiece = chessBoard.getPieceAtPosition(clickedPosition);

        if (clickedPiece == null && pieceCurrentlyHeld == null) {
            return;
        }
        if (clickedPiece == null && pieceCurrentlyHeld != null) {
            attemptPiecePlacement(clickedPosition);
            return;
        }
        if (clickedPiece != null && pieceCurrentlyHeld == null) {
            attemptToPickUpPiece(clickedPiece);
            return;
        }
        attemptToCaptureSquare(clickedPosition);
    }

    private void attemptPiecePlacement(Position clickedPosition) {

        if (!possibleMoves.contains(clickedPosition)) {
            return;
        }

        if (gcState.getMoveNumber() == 0 && undoMove.getHighestMoveNumber() == 0) {
            stalemateChecker.addChessBoardMoment(captureCurrentMoment());
        }
        gcState = gcState.clone();
        chessBoard.setChessPieces(chessBoard.getChessPiecesClone());
        pieceCurrentlyHeld = pieceCurrentlyHeld.clone();
        gcState.moveNumber++;

        moveCurrentlyHeldPiece(clickedPosition);

        if (pieceCurrentlyHeld instanceof Pawn
                || stalemateChecker.getPreviousNumberOfChessPieces()
                != chessBoard.getNumberOfChessPieces()) {
            stalemateChecker.resetToFiftyMoves();
        } else {
            stalemateChecker.decrementRemainingMoveNumber();
        }

        stalemateChecker.setPreviousNumberOfChessPieces(chessBoard.getNumberOfChessPieces());

        nullifyPieceAndPossibleMoves();

        gcState.checkBlockingMoves = null;
        gcState.currentPlayerToMove = (gcState.getCurrentPlayerToMove() == Colour.WHITE) ? Colour.BLACK : Colour.WHITE;

        // This sets currentPlayerIsInCheck to true or false, and toggles the check flag.
        determineIfCurrentPlayerIsInCheck();

        if (gcState.currentPlayerIsInCheck && isCheckmate()) {
            // isCheckmate() populates gcState.getCheckBlockingMoves()
            endGame.declareWinnerByCheckmate(gcState.getCurrentPlayerToMove() == Colour.WHITE ?
                    Colour.BLACK : Colour.WHITE);
        }

        undoMove.setHighestMoveNumber(getMoveNumber());
        ChessBoardMoment currentMoment = captureCurrentMoment();
        stalemateChecker.addChessBoardMoment(currentMoment);

        if (!gcState.currentPlayerIsInCheck)
            checkForStalemate();
    }

    private void moveCurrentlyHeldPiece(Position clickedPosition) {
        Position positionOfPawnAfterEnPassant = null;
        if (gcState.getEnPassantPosition() != null) {
            positionOfPawnAfterEnPassant = Position.createPosition(gcState.enPassantPosition.getXCoord(),
                    (gcState.getCurrentPlayerToMove() == Colour.WHITE) ? gcState.enPassantPosition.getYCoord() + 1 : gcState.enPassantPosition.getYCoord() - 1);
        }
        Position localEnPassantPositionCopy = gcState.enPassantPosition;

        // This is set to null each time because the enPassantPosition only has a lifetime of 1 move.
        // If the opponent does not immediately initiate en passant, the opportunity is lost.
        gcState.enPassantPosition = null;

        if (pieceCurrentlyHeld instanceof King &&
                Math.abs(pieceCurrentlyHeld.getPosition().getXCoord() - clickedPosition.getXCoord()) == 2) {
            performCastling(clickedPosition);
        } else {
            if (pieceCurrentlyHeld instanceof Pawn && clickedPosition.equals(positionOfPawnAfterEnPassant)) {
                // Remove the piece from the board including its image
                chessBoard.removePiece(localEnPassantPositionCopy);
            }
            // If the pawn has moved a distance of 2, it is at risk of being taken by en passant
            else if (pieceCurrentlyHeld instanceof Pawn &&
                    Math.abs(pieceCurrentlyHeld.getPosition().getYCoord() - clickedPosition.getYCoord()) == 2) {
                gcState.enPassantPosition = clickedPosition;
            }

            resetColoursAfterMove();
            chessBoard.movePiece(pieceCurrentlyHeld, clickedPosition);
            chessBoard.setPieceAtPosition(clickedPosition, pieceCurrentlyHeld);
        }

        pieceCurrentlyHeld.markAsHavingMoved();

        // If the piece that moved is a Pawn now on the final square, the user must choose a replacement piece
        if (pieceCurrentlyHeld instanceof Pawn &&
                clickedPosition.getYCoord() == ((gcState.getCurrentPlayerToMove() == Colour.WHITE) ? 8 : 1)) {
            chessBoard.replacePawnWithUserChoice((Pawn) pieceCurrentlyHeld, clickedPosition);
        }
    }

    private void attemptToPickUpPiece(Piece clickedPiece) {
        if (clickedPiece.getColour() != gcState.currentPlayerToMove) {
            return;
        }
        pieceCurrentlyHeld = clickedPiece;
        chessBoard.pieceToChessArraySquare(clickedPiece.getPosition()).setBackground(Color.BLUE);
        this.possibleMoves = getAllowedMovesForPiece(clickedPiece);

        if (gcState.currentPlayerIsInCheck && !(clickedPiece instanceof King)) {
            List<Position> possibleMovesThatBlockCheck = new ArrayList<>();
            for (Position checkBlockingPosition : gcState.checkBlockingMoves) {
                if (this.possibleMoves.contains(checkBlockingPosition)) {
                    possibleMovesThatBlockCheck.add(checkBlockingPosition);
                }
            }
            this.possibleMoves = possibleMovesThatBlockCheck;

            if (gcState.getCheckBlockingMoves() != null && gcState.checkBlockingMoves.contains(gcState.enPassantPosition)) {
                if (clickedPiece instanceof Pawn &&
                        ((Pawn) clickedPiece).adjacentToEnPassantPosition(gcState.enPassantPosition)) {
                    possibleMoves.add(((Pawn) clickedPiece).finalPositionAfterEnPassant(gcState.enPassantPosition));
                }
            }
        }

        for (Position position : possibleMoves) {
            chessBoard.pieceToChessArraySquare(position).setBackground(Color.GREEN);
        }
    }

    private List<Position> cullIllegalMoves(List<List<Position>> initialPossibleMoves,
                                            Piece clickedPiece) {
        List<Position> possibleMoves;
        possibleMoves = new ArrayList<>();
        List<Position> checkedLine;
        if ((checkedLine = getCheckedLine(clickedPiece)) != null) {
            possibleMoves = addPositionsOnCheckedLine(initialPossibleMoves, checkedLine);
        } else {
            for (List<Position> moveList : initialPossibleMoves) {
                for (Position proposedPosition : moveList) {
                    if (chessBoard.getPieceAtPosition(proposedPosition) != null) {
                        if (chessBoard.getPieceAtPosition(proposedPosition).getColour()
                                != gcState.currentPlayerToMove) {
                            possibleMoves.add(proposedPosition);
                        }
                        break;
                    }
                    possibleMoves.add(proposedPosition);
                }
            }
        }
        cullSpecialCases(possibleMoves, clickedPiece);
        return possibleMoves;
    }

    public List<Position> getAllowedMovesForPiece(Piece chessPiece) {
        List<Position> possibleMoves;
        List<List<Position>> initialPossibleMoves = chessPiece.deriveAllMoves();
        possibleMoves = cullIllegalMoves(initialPossibleMoves, chessPiece);
        addSpecialCases(possibleMoves, chessPiece);
        return possibleMoves;
    }

    private List<Position> getCheckedLine(Piece clickedPiece) {
        King ownKing = chessBoard.getKing(gcState.currentPlayerToMove);
        boolean isDiagonalLine = false;
        List<Position> line = calculateSharedOpenLine(ownKing, clickedPiece);
        if (line == null)
            return null;
        for (Position p : line) {
            Piece threateningPiece = chessBoard.getPieceAtPosition(p);
            if (threateningPiece != null && threateningPiece.getColour() != gcState.currentPlayerToMove) {
                if (isDiagonalLine) {
                    if (threateningPiece instanceof Bishop || threateningPiece instanceof Queen)
                        return line;
                } else {
                    if (threateningPiece instanceof Rook || threateningPiece instanceof Queen)
                        return line;
                }
                return null;
            }
        }
        return null;
    }

    private List<Position> calculateSharedOpenLine(King king, Piece piece) {
        int xDiff = piece.getPosition().getXCoord() - king.getPosition().getXCoord();
        int yDiff = piece.getPosition().getYCoord() - king.getPosition().getYCoord();
        if (!(xDiff == 0 || yDiff == 0 || Math.abs(xDiff) == Math.abs(yDiff)))
            return null;

        int xInc, yInc;

        boolean isDiagonalLine = xDiff != 0 && yDiff != 0;

        if (xDiff > 0)
            xInc = 1;
        else if (xDiff == 0)
            xInc = 0;
        else
            xInc = -1;

        if (yDiff > 0)
            yInc = 1;
        else if (yDiff == 0)
            yInc = 0;
        else
            yInc = -1;

        boolean passedThroughPieceFlag = false;
        List<Position> retLine = new ArrayList<>();
        Position positionToAdd = king.getPosition();
        while (true) {
            int xCoord = positionToAdd.getXCoord();
            int yCoord = positionToAdd.getYCoord();
            xCoord += xInc;
            yCoord += yInc;
            positionToAdd = Position.createPosition(xCoord, yCoord);
            if (positionToAdd == null) {
                // Break if the line is now extending off the board
                break;
            }
            retLine.add(positionToAdd);

            // Once the line has passed through the piece, it may carry on until it hits another piece
            if (chessBoard.getPieceAtPosition(positionToAdd) != null) {
                if (passedThroughPieceFlag) {
                    break;
                }
                if (chessBoard.getPieceAtPosition(positionToAdd) != piece) {
                    // If the first piece on the line is not the piece in question, then the piece
                    // does not lie on a checked line.
                    return null;
                }
                passedThroughPieceFlag = true;
            }
        }
        return retLine;
    }

    private List<Position> addPositionsOnCheckedLine(List<List<Position>> initialPossibleMoves,
                                                     List<Position> checkedLine) {
        List<Position> possibleMoves = new ArrayList<>();
        for (List<Position> positionList : initialPossibleMoves) {
            for (Position position : positionList) {
                if (checkedLine.contains(position)) {
                    possibleMoves.add(position);
                }
            }
        }
        return possibleMoves;
    }

    private void cullSpecialCases(List<Position> possibleMoves,
                                  Piece clickedPiece) {
        Position clickedPiecePosition = clickedPiece.getPosition();
        Set<Position> positionsToDelete = new HashSet<>();
        if (clickedPiece instanceof Pawn) {
            int forwardMove = (gcState.getCurrentPlayerToMove() == Colour.WHITE) ? 1 : -1;

            Position forwardPosition = Position.createPosition(clickedPiecePosition.getXCoord(),
                    clickedPiecePosition.getYCoord() + forwardMove);
            if (chessBoard.getPieceAtPosition(forwardPosition) != null) {
                positionsToDelete.add(forwardPosition);
                positionsToDelete.add(Position.createPosition(clickedPiecePosition.getXCoord(),
                        clickedPiecePosition.getYCoord() + 2 * forwardMove));
            }

            Position forwardPosition2 = Position.createPosition(clickedPiecePosition.getXCoord(),
                    clickedPiecePosition.getYCoord() + 2 * forwardMove);
            if (chessBoard.getPieceAtPosition(forwardPosition2) != null) {
                positionsToDelete.add(forwardPosition2);
            }

            for (int xDisp = -1; xDisp < 2; xDisp += 2) {
                Position forwardDiagonal = Position.createPosition(clickedPiecePosition.getXCoord() + xDisp,
                        clickedPiecePosition.getYCoord() + forwardMove);
                Piece potentialVictim = chessBoard.getPieceAtPosition(forwardDiagonal);
                if (potentialVictim == null || potentialVictim.getColour() == gcState.currentPlayerToMove) {
                    positionsToDelete.add(forwardDiagonal);
                }
            }
        }

        if (clickedPiece instanceof King) {
            for (Position position : possibleMoves) {
                if (positionIsChecked(position)) {
                    positionsToDelete.add(position);
                }
            }
        }

        for (Position delPosition : positionsToDelete) {
            possibleMoves.remove(delPosition);
        }
    }

    private void addSpecialCases(List<Position> possibleMoves, Piece clickedPiece) {
        Position clickedPiecePos = clickedPiece.getPosition();
        if (clickedPiece instanceof King) {
            if (clickedPiece.hasMoved() || gcState.currentPlayerIsInCheck)
                return;

            Position reqKingPosition = Position.createPosition(King.KING_POS,
                    (gcState.getCurrentPlayerToMove() == Colour.WHITE) ? 1 : 8);
            if (!clickedPiecePos.equals(reqKingPosition))
                return;

            for (int direction = -1; direction < 2; direction += 2) {
                Position rookPosition = Position.createPosition((direction == -1) ? 1 : 8,
                        clickedPiecePos.getYCoord());
                if (canCastleBetweenPositions(rookPosition, direction)) {
                    possibleMoves.add(Position.createPosition(King.KING_POS + 2 * direction,
                            clickedPiecePos.getYCoord()));
                }
            }
        }

        if (gcState.getEnPassantPosition() != null && clickedPiece instanceof Pawn) {
            Pawn clickedPawn = (Pawn) clickedPiece;
            int requiredYCoord = (gcState.getCurrentPlayerToMove() == Colour.WHITE) ? 5 : 4;
            if (clickedPiecePos.getYCoord() != requiredYCoord)
                return;

            if (clickedPawn.adjacentToEnPassantPosition(gcState.enPassantPosition)) {
                possibleMoves.add(clickedPawn.finalPositionAfterEnPassant(gcState.enPassantPosition));
            }
        }
    }

    private void replacePiece(Position clickedPosition) {
        resetColoursAfterMove();
        pieceCurrentlyHeld = null;
        possibleMoves = null;
    }

    private void attemptToCaptureSquare(Position clickedPosition) {
        if (clickedPosition.equals(pieceCurrentlyHeld.getPosition())) {
            replacePiece(clickedPosition);
            return;
        }
        if (chessBoard.getPieceAtPosition(clickedPosition).getColour() == gcState.currentPlayerToMove) {
            return;
        }
        attemptPiecePlacement(clickedPosition);
    }

    private boolean positionIsChecked(Position position) {
        List<Position> knightAttackPositions = Knight.getKnightAttackPositions(position);
        for (Position knightPosition : knightAttackPositions) {
            Piece possibleKnight = chessBoard.getPieceAtPosition(knightPosition);
            if (possibleKnight == null)
                continue;
            if (possibleKnight instanceof Knight && possibleKnight.getColour() != gcState.currentPlayerToMove)
                return true;
        }

        List<List<Position>> threateningLines = getThreateningLines(position);
        return threateningLines.size() > 0;
    }

    private List<List<Position>> getThreateningLines(Position position) {
        int x, y;
        List<List<Position>> listHolder = new ArrayList<>();
        for (int xDisp = -1; xDisp < 2; xDisp++) {
            for (int yDisp = -1; yDisp < 2; yDisp++) {
                List<Position> line = new ArrayList<>();
                if (xDisp == 0 && yDisp == 0) {
                    continue;
                }
                x = position.getXCoord() + xDisp;
                y = position.getYCoord() + yDisp;
                for (; ; x += xDisp, y += yDisp) {
                    Position nextPosition = Position.createPosition(x, y);
                    if (nextPosition == null) {
                        break;
                    }
                    Piece threateningPiece = chessBoard.getPieceAtPosition(nextPosition);

                    if (threateningPiece == null ||
                            (threateningPiece.getColour() == gcState.getCurrentPlayerToMove() && threateningPiece instanceof King)) {
                        line.add(nextPosition);
                        continue;
                    }

                    if (threateningPiece.getColour() == gcState.currentPlayerToMove) {
                        break;
                    }

                    if (xDisp == 0 || yDisp == 0) {
                        if (threateningPiece instanceof Rook || threateningPiece instanceof Queen
                                || (threateningPiece instanceof King && (Math.abs(position.getXCoord() - x) == 1
                                || Math.abs(position.getYCoord() - y) == 1))) {
                            line.add(nextPosition);
                            listHolder.add(line);
                        }
                    } else {
                        if (threateningPiece instanceof Bishop || threateningPiece instanceof Queen
                                || (threateningPiece instanceof King && (Math.abs(position.getXCoord() - x) == 1
                                && Math.abs(position.getYCoord() - y) == 1))
                                || threateningPiece instanceof Pawn && isThreateningPawn((Pawn) threateningPiece, position)) {
                            line.add(nextPosition);
                            listHolder.add(line);
                        }
                    }
                    break;
                }
            }
        }

        return listHolder;
    }

    private boolean isThreateningPawn(
            Pawn pawn, Position position) {
        Position pawnPosition = pawn.getPosition();
        int xDiff = pawnPosition.getXCoord() - position.getXCoord();
        int yDiff = pawnPosition.getYCoord() - position.getYCoord();
        if (Math.abs(xDiff) != 1 || Math.abs(yDiff) != 1) {
            return false;
        }

        return pawn.getColour() == Colour.BLACK && yDiff == 1
                || pawn.getColour() == Colour.WHITE && yDiff == -1;
    }

    private void resetColoursAfterMove() {
        chessBoard.resetBoardSquareColour(pieceCurrentlyHeld.getPosition());
        for (Position position : possibleMoves) {
            chessBoard.resetBoardSquareColour(position);
        }
    }

    public void determineIfCurrentPlayerIsInCheck() {
        King currentPlayersKing = chessBoard.getKing(gcState.currentPlayerToMove);
        gcState.currentPlayerIsInCheck = positionIsChecked(currentPlayersKing.getPosition());
        chessBoard.toggleCheckLabel(gcState.currentPlayerIsInCheck);
    }

    private void performCastling(Position clickedPosition) {
        Position rookPosition = Position.createPosition((clickedPosition.getXCoord() == 7) ? 8 : 1,
                clickedPosition.getYCoord());
        Piece rookToCastle = chessBoard.getPieceAtPosition(rookPosition);
        Position rookDestination = Position.createPosition((rookPosition.getXCoord() == 1) ? 4 : 6,
                rookPosition.getYCoord());
        rookToCastle.markAsHavingMoved();
        resetColoursAfterMove();
        chessBoard.movePiece(pieceCurrentlyHeld, clickedPosition);
        chessBoard.movePiece(rookToCastle, rookDestination);
    }

    private void checkForStalemate() {
        StalemateOption stalemateOption = stalemateChecker.isStalemate();
        if (stalemateOption == StalemateOption.MANDATORY_PLAYER_CANT_MOVE
                || stalemateOption == StalemateOption.MANDATORY_TOO_FEW_PIECES) {
            endGame.declareMandatoryStalemate(stalemateOption, gcState.currentPlayerToMove);
        } else if (stalemateOption == StalemateOption.OPTIONAL_THREE_FOLD
                || stalemateOption == StalemateOption.OPTIONAL_FIFTY_MOVE) {
            endGame.informThatPlayerMayDeclareStalemate(stalemateOption,
                    gcState.getCurrentPlayerToMove() == Colour.WHITE ? Colour.BLACK : Colour.WHITE);
        }
    }

    private boolean isCheckmate() {
        King kingInCheck = chessBoard.getKing(gcState.currentPlayerToMove);
        List<List<Position>> threateningLines = getThreateningLines(kingInCheck.getPosition());
        List<Position> possibleKingMoves = getAllowedMovesForPiece(kingInCheck);

        if (threateningLines.size() > 1 && possibleKingMoves.size() == 0)
            return true;

        gcState.checkBlockingMoves = new ArrayList<>();
        if (threateningLines.size() == 1) {
            for (Position checkBlockingPosition : threateningLines.get(0)) {
                gcState.checkBlockingMoves.add(checkBlockingPosition);
            }
        }

        List<Position> knightAttackPositions = Knight.getKnightAttackPositions(kingInCheck.getPosition());
        for (Position knightPosition : knightAttackPositions) {
            Piece possibleKnight = chessBoard.getPieceAtPosition(knightPosition);
            if (possibleKnight == null)
                continue;
            if (possibleKnight instanceof Knight && possibleKnight.getColour() != gcState.currentPlayerToMove)
                gcState.checkBlockingMoves.add(knightPosition);
        }

        if (possibleKingMoves.size() > 0) {
            return false;
        }

        List<Piece> currentPlayersPieces = chessBoard.getPlayersPieces(gcState.currentPlayerToMove);
        for (Piece chessPiece : currentPlayersPieces) {
            if (chessPiece instanceof King)
                continue;
            List<Position> allowedMoves = getAllowedMovesForPiece(chessPiece);

            for (Position checkBlockingMove : gcState.checkBlockingMoves) {
                if (allowedMoves.contains(checkBlockingMove)) {
                    return false;
                }
            }
        }

        if (gcState.checkBlockingMoves.size() == 1 && gcState.checkBlockingMoves.get(0).equals(gcState.enPassantPosition)) {
            for (int i = -1; i < 2; i += 2) {
                Position potentialFriendlyPawnPosition = Position.createPosition(gcState.enPassantPosition.getXCoord() + i,
                        gcState.enPassantPosition.getYCoord());
                Piece friendlyPawn = chessBoard.getPieceAtPosition(potentialFriendlyPawnPosition);
                if (!(friendlyPawn instanceof Pawn))
                    continue;
                List<Position> pawnAllowedMoves = getAllowedMovesForPiece(friendlyPawn);
                if (pawnAllowedMoves.contains(((Pawn) friendlyPawn).finalPositionAfterEnPassant(gcState.enPassantPosition)))
                    return false;
            }
        }

        return true;
    }

    public Board getChessBoard() {
        return chessBoard;
    }

    public Colour getCurrentPlayerToMove() {
        return gcState.currentPlayerToMove;
    }

    public ChessBoardMoment captureCurrentMoment() {
        Map<Position, Piece> chessPieces = chessBoard.getChessPiecesClone();
        CastlingOpportunities castlingOpportunities = constructCastlingOpportunities();
        CastlingPiecesMovementTracker castlingPiecesMovementTracker
                = constructCastlingPiecesMovementTracker();
        GameControllerStateInfo clonedGCState = new GameControllerStateInfo(
                gcState.currentPlayerToMove,
                duplicateArrayList(gcState.checkBlockingMoves),
                gcState.currentPlayerIsInCheck,
                gcState.enPassantPosition,
                gcState.moveNumber);

        return new ChessBoardMoment(chessPieces, castlingOpportunities,
                castlingPiecesMovementTracker, clonedGCState);
    }

    private CastlingOpportunities constructCastlingOpportunities() {
        Boolean[] castlingOpportunitiesArray = new Boolean[4];
        for (int i = 0; i < 4; i++) {
            castlingOpportunitiesArray[i] = false;
        }
        int counter = 0;

        gcState.currentPlayerToMove = (gcState.currentPlayerToMove == Colour.WHITE) ? Colour.BLACK : Colour.WHITE;

        for (int yCoord = 1; yCoord <= 8; yCoord += 7) {
            if (gcState.getCurrentPlayerToMove() == Colour.WHITE && yCoord == 8
                    || gcState.getCurrentPlayerToMove() == Colour.BLACK && yCoord == 1) {
                castlingOpportunitiesArray[counter] = null;
                counter++;
                castlingOpportunitiesArray[counter] = null;
                counter++;
                continue;
            }
            Position reqKingPosition = Position.createPosition(King.KING_POS, yCoord);
            Piece supposedKing = chessBoard.getPieceAtPosition(reqKingPosition);
            if (!(supposedKing instanceof King)
                    || (supposedKing != null && supposedKing.hasMoved())) {
                counter += 2;
                continue;
            }
            for (int direction = -1; direction < 2; direction += 2, counter++) {
                Position rookPosition = Position.createPosition((direction == -1) ? 1 : 8,
                        yCoord);
                if (canCastleBetweenPositions(rookPosition, direction)) {
                    castlingOpportunitiesArray[counter] = true;
                }
            }
        }

        gcState.currentPlayerToMove = (gcState.getCurrentPlayerToMove() == Colour.WHITE) ? Colour.BLACK : Colour.WHITE;

        return new CastlingOpportunities(castlingOpportunitiesArray[0], castlingOpportunitiesArray[1], castlingOpportunitiesArray[3], castlingOpportunitiesArray[2]);
    }

    private boolean canCastleBetweenPositions(Position rookPosition, int direction) {
        Piece potentialRook = chessBoard.getPieceAtPosition(rookPosition);
        if (potentialRook == null || !(potentialRook instanceof Rook) || potentialRook.hasMoved()) {
            return false;
        }
        for (int i = 1; i < 3; i++) {
            Position nextSquare = Position.createPosition(King.KING_POS + i * direction,
                    rookPosition.getYCoord());
            if (chessBoard.getPieceAtPosition(nextSquare) != null || positionIsChecked(nextSquare))
                return false;
        }
        return direction != -1 ||
                chessBoard.getPieceAtPosition(Position.createPosition(2, rookPosition.getYCoord())) == null;
    }

    private CastlingPiecesMovementTracker constructCastlingPiecesMovementTracker() {
        boolean[] inputs = new boolean[6];
        int counter = 0;
        for (int yCoord = 1; yCoord <= 8; yCoord += 7) {
            for (int xCoord = 1; xCoord != 12; xCoord += 4, counter++) {
                if (xCoord == 9) {
                    xCoord = 8;
                }
                String requiredPiece = (xCoord == 5) ? "King" : "Rook";
                Position position = Position.createPosition(xCoord, yCoord);
                Piece pieceAtPosition = chessBoard.getPieceAtPosition(position);
                if (pieceAtPosition != null && pieceAtPosition.getName().contains(requiredPiece)
                        && !pieceAtPosition.hasMoved())
                    inputs[counter] = true;
            }
        }
        return new CastlingPiecesMovementTracker(inputs);
    }

    private List<Position> duplicateArrayList(List<Position> listToDuplicate) {
        List<Position> retList = new ArrayList<>();
        if (listToDuplicate == null)
            return retList;
        for (Position entry : listToDuplicate) {
            retList.add(entry);
        }
        return retList;
    }

    public void nullifyPieceAndPossibleMoves() {
        pieceCurrentlyHeld = null;
        possibleMoves = null;
    }

    public Checker getChecker() {
        return stalemateChecker;
    }

    public GameControllerStateInfo getGcState() {
        return gcState;
    }

    public void setGcState(GameControllerStateInfo gcState) {
        this.gcState = gcState;
    }

    public int getMoveNumber() {
        return gcState.moveNumber;
    }

    public void undo() {
        undoMove.undo();
    }

    public int getHighestRecordedMoveNumber() {
        return undoMove.getHighestMoveNumber();
    }

    public Piece getPieceCurrentlyHeld() {
        return pieceCurrentlyHeld;
    }

    public void setPieceCurrentlyHeld(Piece pieceCurrentlyHeld) {
        this.pieceCurrentlyHeld = pieceCurrentlyHeld;
    }

    public List<Position> getPossibleMoves() {
        return possibleMoves;
    }

    public void setPossibleMoves(List<Position> possibleMoves) {
        this.possibleMoves = possibleMoves;
    }

}
