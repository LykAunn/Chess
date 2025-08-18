package main;

import Pieces.*;
import Tile.TileManager;
import logic.Board;
import logic.GameObserver;
import logic.GameState;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class GamePanel extends JPanel implements Runnable, GameObserver {

    // Tile Settings
    public int originalTileSize = 20;
    public int scale = 5;

    // Scaling
    public int tileSize = originalTileSize * scale;
    public int screenWidth = tileSize * 8;
    public int screenHeight = tileSize * 8;

    // UI State
    private int selectedRow = -1, selectedCol = -1;
    private boolean[][] possibleMoves;
    public boolean pieceSelected = false;

    private Board gameBoard;

    // Managers
    private final TileManager tileManager;
    private final PieceManager pieceManager;
    Thread gameThread;
    final int fps = 60;

    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        possibleMoves = new boolean[8][8];

        // Initialize game board and register as observer
        gameBoard = new Board();
        gameBoard.setObserver(this);

        tileManager = new TileManager(this);
        pieceManager = new PieceManager(this);
        tileManager.getTileImage();

        Mouse mouse = new Mouse(this);
        addMouseListener(mouse);
    }

    //Game Observer Implementation
    public void onMoveExecuted(Move move) {
        System.out.println("Move executed: " + move.toString());
        clearSelection();
        repaint();
    }

    public void onPieceSelected(int row, int col, ArrayList<Move> moves) {
        selectedRow = row;
        selectedCol = col;
        pieceSelected = true;

        clearDot();
        for (Move move : moves) {
            possibleMoves[move.getEndRow()][move.getEndCol()] = true;
        }

        repaint();
    }

    public void onSelectionCleared() {
        clearSelection();
        repaint();
    }

    public void onGameStateChanged(GameState newState) {
        System.out.println("Game state changed: " + newState.toString());
    }

    public void onTurnChanged(int currentPlayer) {
        String currentColor = currentPlayer == 0 ? "White" : "Black";
        System.out.println("Turn changed: " + currentColor);
    }

    public void handleClick(int mouseX, int mouseY) {
        int clickedCol = mouseX / tileSize;
        int clickedRow = mouseY / tileSize;

        if (!isValidSquare(clickedRow, clickedCol)) return;

        Piece clickedPiece = gameBoard.getPiece(clickedRow, clickedCol);

        if (pieceSelected) {
            // Try to move
            if (isPossibleMove(clickedRow, clickedCol)) {
                gameBoard.executeMove(selectedRow, selectedCol, clickedRow, clickedCol);
            }

            // Select different piece
            else if (clickedPiece != null && clickedPiece.getColor() == gameBoard.getCurrentColor()) {
                gameBoard.selectPiece(clickedRow, clickedCol);
            }

            // Clear Selection
            else {
                gameBoard.clearSelection();
            }

        } else {
            if (clickedPiece != null && clickedPiece.getColor() == gameBoard.getCurrentColor()) {
                gameBoard.selectPiece(clickedRow, clickedCol);
            }
        }
    }

    private boolean isValidSquare(int row, int col) {
        return row >= 0 && col >= 0 && row < 8 && col < 8;
    }

    public boolean isPossibleMove(int row, int col) {
        return possibleMoves[row][col];
    }

    public boolean isSelected(int row, int col) {
        return selectedCol == col && selectedRow == row;
    }

    private void clearSelection() {
        selectedCol = -1;
        selectedRow = -1;
        pieceSelected = false;
        clearDot();
    }

    public void clearDot() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                possibleMoves[i][j] = false;
            }
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g; //Convert Graphics class to Graphics2D

        // TILE
        tileManager.render(g2d, gameBoard);
        pieceManager.renderPieces(g2d, gameBoard);

        // DOT
        tileManager.renderDot(g2d, gameBoard);

    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void run() {
        double drawInterval = 1000000000 / fps;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;
        long timer = 0;
        int drawCount = 0;

        while (gameThread != null) {
            currentTime = System.nanoTime();

            delta += (currentTime - lastTime) / drawInterval;
            timer += (currentTime - lastTime);
            lastTime = currentTime;

            if (delta >= 1) {
                repaint();
                delta--;
                drawCount++;
            }

            if (timer > 1000000000) {
                System.out.println("FPS: " + fps);
                drawCount = 0;
                timer = 0;
            }
        }
    }
}