package com.epam.ik.entity;

import com.epam.ik.GameController;
import com.epam.ik.entity.pieces.impl.King;
import com.epam.ik.entity.pieces.impl.Pawn;
import com.epam.ik.entity.pieces.Piece;
import com.epam.ik.logic.NewGameChoice;
import com.epam.ik.logic.PawnReplacementChoice;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.*;

import static com.epam.ik.entity.pieces.Piece.Colour;

public class Board extends JFrame {
    private static final int CHESSBOARD_WIDTH = 8;
    private static final int CHESSBOARD_LENGTH = 8;

    private final GameController gc;
    private final Board chessBoard;
    private final JPanel gridJPanel = new JPanel();
    private final JToolBar soleJToolBar = new JToolBar();
    private final JButton newGameButton = new JButton("New game");
    private final JButton undoButton = new JButton("Undo");
    private final JLabel checkNotifier = new JLabel("CHECK  !!!");
    private final JLabel[][] chessSquareArray = new JLabel[CHESSBOARD_LENGTH][CHESSBOARD_WIDTH];

    private Map<Position, Piece> chessPieces;

    public Board(final GameController gc) {
        this.gc = gc;
        chessBoard = this;
        setSize(800, 800);
        JPanel contentPanel = new JPanel();
        setContentPane(contentPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        contentPanel.setLayout(new BorderLayout());
        initializeGridJPanel();
        contentPanel.add(gridJPanel, BorderLayout.CENTER);
        initializeSoleJToolBar();
        contentPanel.add(soleJToolBar, BorderLayout.NORTH);

        newGameButton.addActionListener(actionEvent -> {
            NewGameChoice sole = new NewGameChoice(chessBoard);
            sole.show();
        });

        undoButton.addActionListener(actionEvent -> {
            gc.undo();
            if (gc.getMoveNumber() == 0) {
                undoButton.setEnabled(false);
            }
        });

        initializeChessSquareArray();

        setVisible(true);
    }

    public static Colour getColourOfSquareAtPosition(Position position) {
        int xCoord = position.getXCoord();
        int yCoord = position.getYCoord();
        if (Math.abs(xCoord - yCoord) % 2 == 0) {
            return Colour.BLACK;
        } else {
            return Colour.WHITE;
        }
    }

    public void reset() {
        gc.reset();
        clearJLabels();
        resetAllBoardSquareColours();
        initialiseBoard();
        toggleCheckLabel(false);
        undoButton.setEnabled(false);
    }

    public void resetAllBoardSquareColours() {
        for (int i = 0; i < CHESSBOARD_LENGTH; i++) {
            for (int j = 0; j < CHESSBOARD_WIDTH; j++) {
                JLabel square = chessSquareArray[i][j];
                if (square.getName().charAt(0) == 'g') {
                    square.setBackground(new Color(210, 105, 30));
                } else {
                    square.setBackground(Color.WHITE);
                }
            }
        }
    }

    public void initialiseBoard() {
        chessPieces = new HashMap<>();
        addInitialSixteenPieces();
        Set<Position> positionSet = chessPieces.keySet();
        for (Position position : positionSet) {
            Piece cp = chessPieces.get(position);
            paintBoardSquare(cp.getName(), position);
        }
    }

    /*
     * Called from GameController when the new game button is clicked.
     */
    public void clearJLabels() {
        Set<Position> piecePositionSet = chessPieces.keySet();
        for (Position piecePosition : piecePositionSet) {
            pieceToChessArraySquare(piecePosition).setIcon(null);
        }
    }

    public Piece getPieceAtPosition(Position position) {
        return chessPieces.get(position);
    }

    public void setPieceAtPosition(Position position, Piece newPiece) {
        chessPieces.put(position, newPiece);
    }

    public JLabel pieceToChessArraySquare(Position position) {
        int z = position.getXCoord();
        int xCoord = position.getYCoord();
        int yCoord = z;
        xCoord--;
        yCoord--;
        xCoord = 7 - xCoord;
        return chessSquareArray[xCoord][yCoord];
    }

    public King getKing(Colour currentPlayerToMove) {
        Set<Position> chessPieceSet = chessPieces.keySet();
        for (Position position : chessPieceSet) {
            Piece chessPiece = chessPieces.get(position);
            if (chessPiece instanceof King && chessPiece.getColour() == currentPlayerToMove) {
                return (King) chessPiece;
            }
        }
        return null;
    }

    public void resetBoardSquareColour(Position position) {
        JLabel square = pieceToChessArraySquare(position);
        if (square.getName().charAt(0) == 'g') {
            square.setBackground(new Color(210, 105, 30));
        } else
            square.setBackground(Color.WHITE);
    }

    public void movePiece(Piece pieceCurrentlyHeld, Position clickedPosition) {
        removePiece(pieceCurrentlyHeld.getPosition());
        pieceCurrentlyHeld.setPosition(clickedPosition);
        addPiece(pieceCurrentlyHeld);
        undoButton.setEnabled(true);
    }

    public void removePiece(Position piecePosition) {
        pieceToChessArraySquare(piecePosition).setIcon(null);
        chessPieces.remove(piecePosition);
    }

    public void addPiece(Piece pieceToAdd) {
        setPieceAtPosition(pieceToAdd.getPosition(), pieceToAdd);
        paintBoardSquare(pieceToAdd.getName(), pieceToAdd.getPosition());
    }

    public void toggleCheckLabel(boolean flag) {
        checkNotifier.setVisible(flag);
    }

    public java.util.List<Piece> getPlayersPieces(Colour currentPlayerToMove) {
        List<Piece> currentPlayersPieces = new ArrayList<>();

        Set<Position> keySet = chessPieces.keySet();
        for (Position position : keySet) {
            Piece fetchedPiece = chessPieces.get(position);
            if (fetchedPiece.getColour() == currentPlayerToMove) {
                currentPlayersPieces.add(fetchedPiece);
            }
        }
        return currentPlayersPieces;
    }

    public void replacePawnWithUserChoice(Pawn pieceCurrentlyHeld,
                                          Position clickedPosition) {
        PawnReplacementChoice sole = new PawnReplacementChoice(chessBoard, gc, pieceCurrentlyHeld, clickedPosition);
        sole.replace();
    }

    public int getNumberOfChessPieces() {
        return chessPieces.size();
    }

    public Map<Position, Piece> getChessPiecesClone() {
        Map<Position, Piece> chessPiecesClone = new HashMap<>();
        Set<Position> keySet = chessPieces.keySet();
        for (Position position : keySet) {
            chessPiecesClone.put(position, chessPieces.get(position).clone());
        }

        return chessPiecesClone;
    }

    public Map<Position, Piece> getChessPieces() {
        return chessPieces;
    }

    public void setChessPieces(Map<Position, Piece> chessPieces) {
        this.chessPieces = chessPieces;
    }

    private void initializeGridJPanel() {
        GridLayout gridLayout = new GridLayout();
        gridLayout.setRows(CHESSBOARD_LENGTH);
        gridLayout.setColumns(CHESSBOARD_WIDTH);
        gridJPanel.setLayout(gridLayout);
    }

    private void initializeSoleJToolBar() {
        soleJToolBar.setOrientation(JToolBar.HORIZONTAL);
        soleJToolBar.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        soleJToolBar.setFloatable(false);
        soleJToolBar.add(newGameButton);
        soleJToolBar.add(new JToolBar.Separator());
        soleJToolBar.add(undoButton);
        undoButton.setEnabled(false);
        soleJToolBar.add(new JToolBar.Separator());
        soleJToolBar.add(new JToolBar.Separator());
        checkNotifier.setForeground(new Color(210, 105, 30));
        checkNotifier.setVisible(false);
        soleJToolBar.add(checkNotifier);
    }

    private void initializeChessSquareArray() {
        boolean bool1 = false;
        boolean bool2 = false;

        for (int i = 0; i < CHESSBOARD_LENGTH; i++) {
            for (int j = 0; j < CHESSBOARD_WIDTH; j++) {
                chessSquareArray[i][j] = new JLabel((Icon) null, JLabel.CENTER);
                chessSquareArray[i][j].setOpaque(true);
                if (bool2 ^ bool1) {
                    chessSquareArray[i][j].setBackground(new Color(210, 105, 30));
                    chessSquareArray[i][j].setName("gray" + i + j);
                } else {
                    chessSquareArray[i][j].setBackground(Color.WHITE);
                    chessSquareArray[i][j].setName("white" + i + j);
                }
                gridJPanel.add(chessSquareArray[i][j]);

                bool2 = !bool2;

                chessSquareArray[i][j].addMouseListener(new MouseAdapter() {

                    @Override
                    public void mouseReleased(MouseEvent mouseEvent) {
                        Component currentJLabel = mouseEvent.getComponent();
                        String labelName = currentJLabel.getName();
                        Position clickedPosition = arrayToBoard(labelName.charAt(labelName.length() - 2) - '0',
                                labelName.charAt(labelName.length() - 1) - '0');
                        try {
                            gc.squareClicked(clickedPosition);
                        } catch (CloneNotSupportedException ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            }
            bool1 = !bool1;
        }
    }

    private void addInitialSixteenPieces() {
        Piece.Colour colour;
        int xCoord;
        int yCoord;

        for (int i = 1; i <= 2; i++) {
            colour = (i == 1) ? Colour.WHITE : Colour.BLACK;
            yCoord = (i == 1) ? 2 : 7;
            for (xCoord = 1; xCoord <= CHESSBOARD_WIDTH; xCoord++) {
                Position position = Position.createPosition(xCoord, yCoord);
                setPieceAtPosition(position, Piece.createChessPiece("Pawn", colour, position));
            }
        }

        for (int i = 1; i <= 2; i++) {
            colour = (i == 1) ? Colour.WHITE : Colour.BLACK;
            yCoord = (i == 1) ? 1 : 8;
            for (xCoord = 1; xCoord <= CHESSBOARD_WIDTH; xCoord++) {
                Position position = Position.createPosition(xCoord, yCoord);
                switch (xCoord) {
                    case 1:
                    case 8:
                        setPieceAtPosition(position, Piece.createChessPiece("Rook", colour, position));
                        break;
                    case 2:
                    case 7:
                        setPieceAtPosition(position, Piece.createChessPiece("Knight", colour, position));
                        break;
                    case 3:
                    case 6:
                        setPieceAtPosition(position, Piece.createChessPiece("Bishop", colour, position));
                        break;
                    case 4:
                        setPieceAtPosition(position, Piece.createChessPiece("Queen", colour, position));
                        break;
                    case 5:
                        setPieceAtPosition(position, Piece.createChessPiece("King", colour, position));
                        break;
                }
            }
        }
    }

    private void paintBoardSquare(String pieceName, Position position) {
        pieceToChessArraySquare(position).setIcon(new ImageIcon("src/main/resources/images/" + pieceName + ".gif"));
    }

    private Position arrayToBoard(int xCoord, int yCoord) {
        xCoord = 7 - xCoord;
        xCoord++;
        yCoord++;
        int z = xCoord;
        xCoord = yCoord;
        yCoord = z;
        return Position.createPosition(xCoord, yCoord);
    }


}
