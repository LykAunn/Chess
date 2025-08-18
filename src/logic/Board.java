package logic;

import Pieces.*;

import java.util.ArrayList;

public class Board {
    private GameObserver observer;
    public static Piece[][] board;
    private int currentColor = 0;
    private ArrayList<Move> moveHistory;
    private static Move lastMove;
    private GameState gameState = GameState.PLAYING;
    public boolean whiteOnBottom;

    public Board() {
        board = new Piece[8][8];
        moveHistory = new ArrayList<>();
        initializeBoard();
    }

    public void setObserver(GameObserver observer) {
        this.observer = observer;
    }

    private void notifyObserver() {
        if (observer != null) {
            observer.onGameStateChanged(gameState);
        }
    }

    public void selectPiece(int row, int col) {
        Piece piece = board[row][col];
        if (piece != null && piece.getColor() == currentColor) {
            ArrayList<Move> possibleMoves = piece.getPossibleMoves(board);

            if (observer != null) {
                observer.onPieceSelected(row, col, possibleMoves);
            }
        }
    }

    public boolean executeMove(int fromRow, int fromCol, int toRow, int toCol) {
        Piece piece = board[fromRow][fromCol];
        if (piece == null || !isValidMove(fromRow, fromCol, toRow, toCol)) {
            return false;
        }

        Piece capturedPiece = board[toRow][toCol];

        //En Passant
        if (isEnPassantMove(fromRow, fromCol, toRow, toCol)) {
            capturedPiece = board[fromRow][toCol];
            board[fromRow][toCol] = null;
        }

        board[fromRow][fromCol] = null;
        board[toRow][toCol] = piece;
        piece.row = toRow;
        piece.col = toCol;

        //Record move into Move
        PieceType capturedType = capturedPiece == null ? null : capturedPiece.getType();
        lastMove = new Move(fromRow, fromCol, toRow, toCol, null, capturedType, piece.getType());
        moveHistory.add(lastMove);

        //Switch turns
        currentColor = currentColor == 0 ? 1 : 0;

        if (checked(currentColor)) {
            System.out.println("CHECK ON " + (currentColor == 0 ? "WHITE" : "BLACK") + " move");
        }

        //Notify Observer about the move
        if (observer != null) {
            observer.onMoveExecuted(lastMove);
            observer.onTurnChanged(currentColor);
            displayBoard();
        }

        return true;
    }

    public void clearSelection() {
        if (observer != null) {
            observer.onSelectionCleared();
        }
    }


    //Game logic
    public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol) {
        Piece piece = getPiece(fromRow, fromCol);
        if (piece == null) return false;

        ArrayList<Move> possibleMoves = piece.getPossibleMoves(board);
        for (Move move : possibleMoves) {
            if (move.getEndCol() == toCol && move.getEndRow() == toRow) {
                return true;
            }
        }
        return false;
    }

    public boolean isEnPassantMove(int fromRow, int fromCol, int toRow, int toCol) {
        Piece originalPiece = getPiece(fromRow, fromCol);

        if (originalPiece == null || originalPiece.getType() != PieceType.PAWN) return false;

        return Math.abs(toCol - fromCol) == 1 &&
                Math.abs(toRow - fromRow) == 1 &&
                board[toRow][toCol] == null;
    }

    // Check detection
    public boolean isAttacked(int targetRow, int targetCol) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board[row][col];
                if (piece != null && piece.getColor() != currentColor) {
                    ArrayList<Move> possibleMoves = piece.getPossibleMoves(board);
                    for (Move move : possibleMoves) {
                        if (move.getEndCol() == targetCol && move.getEndRow() == targetRow) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public int[] findKing(int color) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board[row][col];
                if (piece != null && piece.getColor() == color && piece.getType() == PieceType.KING) {
                    return new int[]{row, col};
                }
            }
        }
        return null;
    }

    public boolean checked(int color) {
        int[] kingPos = findKing(color);
        if (kingPos == null) return false;

        int kingRow = kingPos[0];
        int kingCol = kingPos[1];

        int opponentColor = color == 0 ? 1 : 0;
        return isAttacked(kingRow, kingCol);
    }

    public Piece getPiece(int row, int col) {
        return board[row][col];
    }

    public int getCurrentColor() {
        return currentColor;
    }

    public static Move getLastMove() {
        return lastMove;
    }

    public void initializeBoard() {

        whiteOnBottom = true;

        //Black Pieces (top)
        board[0][0] = new ROOK(1, 0, 0, whiteOnBottom);
        board[0][1] = new KNIGHT(1, 0, 1, whiteOnBottom);
        board[0][2] = new BISHOP(1, 0, 2, whiteOnBottom);
        board[0][3] = new QUEEN(1, 0, 3, whiteOnBottom);
        board[0][4] = new KING(1, 0, 4, whiteOnBottom);
        board[0][5] = new BISHOP(1, 0, 5, whiteOnBottom);
        board[0][6] = new KNIGHT(1, 0, 6, whiteOnBottom);
        board[0][7] = new ROOK(1, 0, 7, whiteOnBottom);
        for (int i = 0; i < 8; i++) {
            board[1][i] = new PAWN(1, 1, i, whiteOnBottom);
        }

        //White pieces (bottom)
        board[7][0] = new ROOK(0, 7, 0, whiteOnBottom);
        board[7][1] = new KNIGHT(0, 7, 1, whiteOnBottom);
        board[7][2] = new BISHOP(0, 7, 2, whiteOnBottom);
        board[7][3] = new QUEEN(0, 7, 3, whiteOnBottom);
        board[7][4] = new KING(0, 7, 4, whiteOnBottom);
        board[7][5] = new BISHOP(0, 7, 5, whiteOnBottom);
        board[7][6] = new KNIGHT(0, 7, 6, whiteOnBottom);
        board[7][7] = new ROOK(0, 7, 7, whiteOnBottom);
        for (int i = 0; i < 8; i++) {
            board[6][i] = new PAWN(0, 6, i, whiteOnBottom);
        }
        board[5][5] = new KNIGHT(1, 5, 5, whiteOnBottom);
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
}
