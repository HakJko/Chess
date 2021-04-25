package com.epam.ik.logic;

import com.epam.ik.GameController;
import com.epam.ik.entity.Board;
import com.epam.ik.entity.Position;
import com.epam.ik.entity.pieces.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class PawnReplacementChoice
{
    private Board chessBoard;
    private GameController gameController;
    private JOptionPane optionPane;
    private JDialog dialog;
    private Piece pieceCurrentlyHeld;
    private Position clickedPosition;

    public PawnReplacementChoice(Board chessBoard, GameController gameController,
                                 Piece pieceCurrentlyHeld,Position clickedPosition) {
        this.chessBoard = chessBoard;
        this.gameController = gameController;
        this.pieceCurrentlyHeld = pieceCurrentlyHeld;
        this.clickedPosition = clickedPosition;
    }

    public void replace() {
        JButton[] optionButtons = new JButton[4];
        for (int i = 0; i < 4; i++) {
            String choice = null;
            switch(i) {
                case 0: choice = "Bishop"; break;
                case 1: choice = "Knight"; break;
                case 2: choice = "Queen"; break;
                case 3: choice = "Rook"; break;
            }
            JButton button = new JButton(choice);
            button.addActionListener(generateActionListener(choice));
            optionButtons[i] = button;
        }
        optionPane = new JOptionPane("Choose a piece to replace the pawn.", JOptionPane.QUESTION_MESSAGE,
                JOptionPane.DEFAULT_OPTION, null, optionButtons);

        dialog = new JDialog(chessBoard, true);
        dialog.setContentPane(optionPane);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        optionPane.addPropertyChangeListener(
                e -> {
                    if (dialog.isVisible() && (e.getSource() == optionPane)) {
                        dialog.setVisible(false);
                    }
                });
        dialog.pack();
        dialog.setLocationRelativeTo(chessBoard);
        dialog.setVisible(true);
    }

    private ActionListener generateActionListener(final String choice) {
        return e -> implementPawnReplacementChoice(choice);
    }

    private void implementPawnReplacementChoice(String choice) {
        if (choice.equals("Queen"))
            pieceCurrentlyHeld = new Queen(pieceCurrentlyHeld.getColour(), clickedPosition);
        else if (choice.equals("Knight"))
            pieceCurrentlyHeld = new Knight(pieceCurrentlyHeld.getColour(), clickedPosition);
        else if (choice.equals("Rook"))
            pieceCurrentlyHeld = new Rook(pieceCurrentlyHeld.getColour(), clickedPosition);
        else if (choice.equals("Bishop"))
            pieceCurrentlyHeld = new Bishop(pieceCurrentlyHeld.getColour(), clickedPosition);
        chessBoard.setPieceAtPosition(clickedPosition, pieceCurrentlyHeld);
        chessBoard.movePiece(pieceCurrentlyHeld, clickedPosition);
        gameController.determineIfCurrentPlayerIsInCheck();
        optionPane.firePropertyChange("a", false, true);
    }
}
