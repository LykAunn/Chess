package main;

import Pieces.*;
import Tile.TileManager;

import javax.lang.model.type.ArrayType;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class GamePanel extends JPanel implements Runnable {

    //Tile Settings
    public int originalTileSize = 20;
    public int scale = 5;

    //Scaling
    public int tileSize = originalTileSize * scale;
    public int screenWidth = tileSize * 8;
    public int screenHeight = tileSize * 8;

    //Game Logic
    Thread gameThread;
    public Piece[][] board;
    String playerColor;
    public boolean whiteOnBottom;
    public boolean pieceSelected = false;
    public Move lastMove = null;

    public int currentColor = 0;

    private int selectedRow = -1, selectedCol = -1;
    private boolean[][] possibleMoves;
    private final TileManager tileManager;
    private final PieceManager pieceManager;
    public ArrayList<Move> moveHistory; //Stores all the move history for functions such as capturing etc

    //FPS
    final int fps = 60;

    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        board = new Piece[8][8];
        possibleMoves = new boolean[8][8];
        moveHistory = new ArrayList<>();
        tileManager = new TileManager(this);
        pieceManager = new PieceManager(this);
        tileManager.getTileImage();
        Mouse mouse = new Mouse(this);
        addMouseListener(mouse);
//        possibleMoves[2][2] = true;
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
        setBoard();
        displayBoard();
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

    public void setBoard() {
        whiteOnBottom = true;
        board[0][0] = new ROOK(1, 0, 0, whiteOnBottom);
        board[0][1] = new KNIGHT(1, 0, 1, whiteOnBottom);
        board[0][2] = new BISHOP(1, 0, 2, whiteOnBottom);
        board[0][3] = new QUEEN(1, 0, 3, whiteOnBottom);
        board[0][4] = new KING(1, 0, 4, whiteOnBottom);
        board[0][5] = new BISHOP(1, 0, 5, whiteOnBottom);
        board[0][6] = new KNIGHT(1, 0, 6, whiteOnBottom);
        board[0][7] = new ROOK(1, 0, 7, whiteOnBottom);
        for (int i = 0; i < 8; i++) {
            board[1][i] = new PAWN(1, 1, i, whiteOnBottom, this);
        }

        board[7][0] = new ROOK(0, 7, 0, whiteOnBottom);
        board[7][1] = new KNIGHT(0, 7, 1, whiteOnBottom);
        board[7][2] = new BISHOP(0, 7, 2, whiteOnBottom);
        board[7][3] = new QUEEN(0, 7, 3, whiteOnBottom);
        board[7][4] = new KING(0, 7, 4, whiteOnBottom);
        board[7][5] = new BISHOP(0, 7, 5, whiteOnBottom);
        board[7][6] = new KNIGHT(0, 7, 6, whiteOnBottom);
        board[7][7] = new ROOK(0, 7, 7, whiteOnBottom);
        for (int i = 0; i < 8; i++) {
            board[6][i] = new PAWN(0, 6, i, whiteOnBottom, this);
        }
        board[5][5] = new KNIGHT(1, 5, 5, whiteOnBottom);
//
//        KNIGHT testRook = (KNIGHT) board[5][5];
//        ArrayList<Move> moves = testRook.getPossibleMoves(board);
//        for (Move move : moves) {
//            System.out.println(move.toString());
//        }

//        PAWN myPawn = (PAWN) board[6][6];
//        ArrayList<Move> movestest = myPawn.getPossibleMoves(board);
//        for (Move move : movestest) {
//            System.out.println(move.toString());
//        }
    }

    public void displayBoard() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (board[i][j] != null) {
                    System.out.print("[" + board[i][j].getType() + "] ");
                } else {
                    System.out.print("[ ]");
                }
            }
            System.out.println();
        }
    }

    public Piece getPiece(int row, int col) {
        return board[row][col];
    }

    public boolean isSelected(int row, int col) {
        return row == selectedRow && col == selectedCol;
    }

    public boolean isPossibleMove(int row, int col) {
        return possibleMoves[row][col];
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g; //Convert Graphics class to Graphics2D

        // TILE
        tileManager.render(g2d);
        PieceManager.renderPieces(g2d);

        // DOT
        tileManager.renderDot(g2d);

        //OBJECT
//        for (int i = 0; i < obj.length; i++) {
//            if (obj[i] != null) {
//                obj[i].draw(g2d, this);
//            }
//        }
    }

    public void handleClick(int mouseX, int mouseY) {
        int boardCol = mouseX / tileSize;
        int boardRow = mouseY / tileSize;
        System.out.println("Clicked: " + boardCol + " " + boardRow);

        // Bounds Check
//        if (isValidSquare(boardRow, boardCol)) {
//            System.out.println("OUT OF BOUNDS");
//            return;
//        }

        Piece clickedPiece = getPiece(boardRow, boardCol);

        // Case 1: Already Have a Piece Selected
        if (pieceSelected) {

            // Try to move to clicked Square
            if (isPossibleMove(boardRow, boardCol)) {
                executeMove(selectedRow, selectedCol, boardRow, boardCol);
                clearSelection();
                switchTurns();
            }

            // Clicked on own Piece -> Select the new Piece
            else if (clickedPiece != null && clickedPiece.getColor() == currentColor) {
                selectPiece(boardRow, boardCol, clickedPiece);
            }

            //Clicked elsewhere -> Deselect
            else {
                clearSelection();
            }
        }

        // Case 2: No piece Selected -> Try to select new Piece
        else if (clickedPiece != null && clickedPiece.getColor() == currentColor) {
            System.out.println("Selected piece: " + clickedPiece);
            selectPiece(boardRow, boardCol, clickedPiece);
        }
    }

//        if (boardCol >= 0 && boardRow >= 0 && boardCol < 8 && boardRow < 8) {
//
//            Piece clickedPiece = getPiece(boardRow, boardCol);
//
//            if (clickedPiece != null && clickedPiece.getColor() == currentColor) {
//                selectedCol = boardCol;
//                selectedRow = boardRow;
//                pieceSelected = true;
//
//                ArrayList<Move> moves = clickedPiece.getPossibleMoves(board);
//                handleMove(moves);
//            } else if (pieceSelected && isPossibleMove(boardRow, boardCol)) {
//                if (clickedPiece == null || clickedPiece.getColor() != currentColor) {
//                    executeMove(selectedRow, selectedCol, boardRow, boardCol);
//                    System.out.println("MOVE: " + selectedCol + ", " + selectedRow);
//                    pieceSelected = false;
//                }
//
//            }
//        }

    private boolean isValidSquare(int row, int col) {
        return row >= 0 && col >= 0 && row < 8 && col < 8;
    }

    private void selectPiece(int row, int col, Piece piece) {
        selectedCol = col;
        selectedRow = row;
        pieceSelected = true;

        ArrayList<Move> moves = piece.getPossibleMoves(board);
        handleMove(moves);
    }

    private void clearSelection() {
        selectedCol = -1;
        selectedRow = -1;
        pieceSelected = false;
        clearDot();
    }

    private void switchTurns() {
        currentColor = (currentColor == 0) ? 1 : 0;
    }

    public void handleMove(ArrayList<Move> moves) {
        clearDot();

        for (Move move : moves) {
            int endRow = move.getEndRow();
            int endCol = move.getEndCol();

            possibleMoves[endRow][endCol] = true;
            System.out.println(endRow + " " + endCol);
        }
    }

    public void clearDot() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                possibleMoves[i][j] = false;
            }
        }
    }

    public void executeMove(int originRow, int originCol, int destRow, int destCol) {
        Piece originalPiece = getPiece(originRow, originCol);
        Piece capturedPiece = getPiece(destRow, destCol);

            board[destRow][destCol] = originalPiece;
            board[originRow][originCol] = null;

            originalPiece.row = destRow;
            originalPiece.col = destCol;
            clearSelection();

            displayBoard();

            PieceType capturedPieceType = null;

            if (capturedPiece != null) {
                capturedPieceType = capturedPiece.getType();
            }

            lastMove = new Move(originRow, originCol, destRow, destCol, null, capturedPieceType, originalPiece.getType());
            moveHistory.add(lastMove);

            System.out.println(moveHistory.toString());
    }

    public Move getLastMove() {
        return lastMove;
    }

}