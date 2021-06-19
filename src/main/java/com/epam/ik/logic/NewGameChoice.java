package com.epam.ik.logic;

import com.epam.ik.entity.Board;

import javax.swing.*;

public class NewGameChoice {
    private final Board chessBoard;

    public NewGameChoice(Board chessBoard) {
        this.chessBoard = chessBoard;
    }

    public void show() {
        int n = JOptionPane.showConfirmDialog(
                chessBoard, "Really start a new game?",
                "New game",
                JOptionPane.YES_NO_OPTION);
        if (n == JOptionPane.YES_OPTION) {
            chessBoard.reset();
        }
    }
}
