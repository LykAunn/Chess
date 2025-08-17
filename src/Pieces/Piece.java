package Pieces;

import main.GamePanel;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

public abstract class Piece {

    protected int color;
    protected PieceType type;
    protected BufferedImage image;
    public static final int WHITE = 0;
    public static final int BLACK = 1;

    public int row;
    public int col;
    protected boolean hasMoved; //For castling
    protected boolean isAlive;

    protected boolean isSelected;
    protected boolean whiteOnBottom;
    protected GamePanel gamePanel;

    public abstract ArrayList<Move> getPossibleMoves(Piece[][] board);

    public Piece(int color, PieceType type, int row, int col, boolean whiteOnBottom) {
        this.color = color;
        this.type = type;
        this.row = row;
        this.col = col;
        this.hasMoved = false;
        this.isAlive = true;
        this.isSelected = false;
        this.whiteOnBottom = whiteOnBottom;
    }

    //Getters
    public int getColor() { return color; }

    public PieceType getType() { return type; }

    public int getRow() { return row; }

    public int getCol() { return col; }

    public boolean hasMoved() { return hasMoved; }

    public boolean isAlive() { return isAlive; }

    public boolean isSelected() { return isSelected; }

    //SETTERS
    public void setPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public void setHasMoved(boolean hasMoved) {
        this.hasMoved = hasMoved;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    //GAME STATE
    public void captured() {
        isAlive = false;
    }

    public void revived() {
        isAlive = true;
    }

    //Check for movements
    public boolean isInBound(Piece[][] board, int row, int col) {
        return row >= 0 && row <= 7 && col >= 0 && col <= 7;
    }

    public boolean targetCheck(Piece[][] board, int newRow, int newCol, int color) {
        return board[newRow][newCol] != null && board[newRow][newCol].getColor() != color;
    }

    protected ArrayList<Move> getSlidingMoves(Piece[][] board, int[][] directions) {
        ArrayList<Move> moves = new ArrayList<>();
        for (int[] dir : directions) {
            int rowDir = dir[0];
            int colDir = dir[1];

            for (int i = 1; i <= 7; i++) {
                int newRow = row + (i * rowDir);
                int newCol = col + (i * colDir);

                if (isInBound(board, newRow, newCol)) {
                    if (board[newRow][newCol] == null) {
                        moves.add(new Move(row, col, newRow, newCol, null, null, null));
                    } else if (targetCheck(board, newRow, newCol, color)) {
                        moves.add(new Move(row, col, newRow, newCol, null, null, null));
                        break;
                    } else {
                        break;
                    }
                }
            }
        }
        return moves;
    }

    protected ArrayList<Move> getNonSlidingMoves(Piece[][] board, int[][] directions, int row, int col) {
        ArrayList<Move> moves = new ArrayList<>();
        for (int[] direction : directions) {
            int rowDir = direction[0];
            int colDir = direction[1];

            int newRow = rowDir + row;
            int newCol = colDir + col;

            if (isInBound(board, newRow, newCol)) {
                if (targetCheck(board, newRow, newCol, color) || board[newRow][newCol] == null) {
                    moves.add(new Move(row, col, newRow, newCol, null, null, null));
                }

            }
        }

        return moves;
    }
}