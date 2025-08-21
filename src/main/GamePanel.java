package main;

import Pieces.*;
import Tile.TileManager;
import logic.Board;
import logic.GameObserver;
import logic.GameState;

import javax.sound.midi.SysexMessage;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

import static logic.GameState.TITLE;

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

    // Dragging
    private boolean isDragging = false;
    private Piece draggedPiece = null;
    private int dragSourceRow = -1;
    private int dragSourceCol = -1;
    private int mouseX = 0;
    private int mouseY = 0;

    private Board gameBoard;

    // Managers
    private final TileManager tileManager;
    final PieceManager pieceManager;
    private final UI ui;
    Thread gameThread;
    final int fps = 60;

    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.setFocusable(true);
        possibleMoves = new boolean[8][8];

        // Initialize game board and register as observer
        gameBoard = new Board();
        gameBoard.setObserver(this);

        tileManager = new TileManager(this);
        pieceManager = new PieceManager(this);
        ui = new UI(this);
        tileManager.getTileImage();

        Mouse mouse = new Mouse(this);
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
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
            // Select the piece
            if (clickedPiece != null && clickedPiece.getColor() == gameBoard.getCurrentColor()) {
                gameBoard.selectPiece(clickedRow, clickedCol);
            }
        }
    }

    public void handleMousePressed(int mouseX, int mouseY) {
        int col = mouseX / tileSize;
        int row = mouseY / tileSize;

        if (isValidSquare(row, col)) {
            Piece piece = gameBoard.getPiece(row, col);
            if (piece != null && piece.getColor() == gameBoard.getCurrentColor()) {

                // Store potential drag info
                dragSourceRow = row;
                dragSourceCol = col;
                draggedPiece = piece;
            }
        }
    }

    public void handleDragStart(int x, int y) {
        if (draggedPiece != null && dragSourceRow != -1 && dragSourceCol != -1) {
            isDragging = true;
            mouseX = x;
            mouseY = y;

            // Show possible moves
            gameBoard.selectPiece(dragSourceRow, dragSourceCol);
            repaint();
        }
    }

    public void handleDragUpdate(int x, int y) {
        if (isDragging) {
            mouseX = x;
            mouseY = y;
            repaint();
        }
    }

    public void handleDragEnd(int x, int y) {
        if (isDragging) {
            int col = x / tileSize;
            int row = y / tileSize;

            if (isValidSquare(row, col)) {
                gameBoard.executeMove(dragSourceRow, dragSourceCol, row, col);
            }

            // Reset drag state
            isDragging = false;
            draggedPiece = null;
            dragSourceRow = -1;
            dragSourceCol = -1;
            clearSelection();
            repaint();
        }
    }

    public boolean isDragSource(int row, int col) {
        return isDragging && row == dragSourceRow && col == dragSourceCol;
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

    public void setGameState(GameState gameState) {
        gameBoard.setGameState(gameState);
    }

    public void setPlayerColor(boolean whiteBottom) {
        gameBoard.setPlayerColor(whiteBottom);
        gameBoard.startGame();
    }

    public GameState getGameState() {
        return gameBoard.getGameState();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g; //Convert Graphics class to Graphics2D

        if (gameBoard.getGameState() == TITLE) {
            ui.draw(g2d);
        } else {
            // TILE
            tileManager.render(g2d, gameBoard);
            pieceManager.renderPieces(g2d, gameBoard);

            // DOT
            tileManager.renderDot(g2d, gameBoard);

            // Moving pieces while dragging
            if (isDragging && draggedPiece != null) {
                pieceManager.renderDraggedPiece(g2d, draggedPiece, mouseX, mouseY);
            }
        }


    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void run() {
        double drawInterval = (double) 1000000000 / fps;
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

    public void callUIToHandleClick(int x, int y) {
        ui.handleClick(x, y);
    }
}