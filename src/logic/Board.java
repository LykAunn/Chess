package logic;

import Pieces.*;
import main.Sound;

import java.util.ArrayList;

public class Board {
    private GameObserver observer;
    public static Piece[][] board;
    private int currentColor = 0;
    private ArrayList<Move> moveHistory;
    private static Move lastMove;
    private GameState gameState = GameState.PLAYING;
    public boolean whiteOnBottom;
    Sound sound = new Sound();

    // Flags to track if pieces have moved
    private boolean whiteKingMoved = false;
    private boolean blackKingMoved = false;
    private boolean whiteKingsideRookMoved = false;
    private boolean whiteQueensideRookMoved = false;
    private boolean blackKingsideRookMoved = false;
    private boolean blackQueensideRookMoved = false;

    public Board() {
        board = new Piece[8][8];
        moveHistory = new ArrayList<>();
        initializeBoard();
        playSE(0);
    }

    public void playSE(int i) {

        sound.setFile(i);
        sound.play();
    }

    public void playRandomSE(int i, int j) {

        double random =Math.random();
        if (random < 0.5) {
            playSE(i);
        } else {
            playSE(j);
        }
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

            if (piece.getType() ==  PieceType.KING) {
                possibleMoves.removeIf(move -> isAttacked(move.getEndRow(), move.getEndCol(), currentColor));
                ArrayList<Move> castleMoves = getCastlingMoves(row, col, currentColor);


                possibleMoves.addAll(castleMoves);

            }


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

//        if (piece != null && piece.getType() ==  PieceType.PAWN) {
//            ArrayList<int[]> test = piece.getPawnAttacks(board);
//        }

        //En Passant
        if (isEnPassantMove(fromRow, fromCol, toRow, toCol)) {
            capturedPiece = board[fromRow][toCol];
            board[fromRow][toCol] = null;
        }

        if (isCastleMove(fromRow, fromCol, toRow, toCol)) {
            int whichRow = currentColor == 0 ? 7 : 0;
            if (isRightSideCastle(fromRow, fromCol, toRow, toCol)) {
                Piece castlePiece = board[whichRow][7];
                board[whichRow][7] = null;
                board[whichRow][5] = castlePiece;
            } else {
                Piece castlePiece = board[whichRow][0];
                board[whichRow][0] = null;
                board[whichRow][3] = castlePiece;
            }
        }

        if(isCastleMove(fromRow, fromCol, toRow, toCol)) {
            playRandomSE(5, 6);
        } else if (capturedPiece != null || isEnPassantMove(fromRow, fromCol, toRow, toCol)) {
            playRandomSE(3, 4);
        } else {
            playRandomSE(1, 2);
        }

        updateCastlingFlags(piece, fromRow, fromCol);

        board[fromRow][fromCol] = null;
        board[toRow][toCol] = piece;
        piece.row = toRow;
        piece.col = toCol;

        //Record move into Move
        PieceType capturedType = capturedPiece == null ? null : capturedPiece.getType();
        lastMove = new Move(fromRow, fromCol, toRow, toCol, null, capturedType, piece.getType(), currentColor);
        moveHistory.add(lastMove);

        if (checked(currentColor)) {
            System.out.println("CHECK ON " + (currentColor == 0 ? "WHITE" : "BLACK") + " move");
            gameState = GameState.CHECK;

            playRandomSE(7, 8);
            if (observer != null) {
                observer.onGameStateChanged(gameState);
                notifyObserver();
            }
        }

        if (checkMate(currentColor)) {
            gameState = GameState.CHECKMATE;

            playSE(9);
            if (observer != null) {
                observer.onGameStateChanged(gameState);
                notifyObserver();
            }
        }

        //Switch turns
        currentColor = currentColor == 0 ? 1 : 0;


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
        if (piece.getType() ==  PieceType.KING) {
            possibleMoves.removeIf(move -> isAttacked(move.getEndRow(), move.getEndCol(), currentColor));
            ArrayList<Move> castleMoves = getCastlingMoves(fromRow, fromCol, currentColor);


            possibleMoves.addAll(castleMoves);

        }
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

    public boolean isCastleMove(int fromRow, int fromCol, int toRow, int toCol) {
        Piece originalPiece = getPiece(fromRow, fromCol);

        if (originalPiece == null || originalPiece.getType() != PieceType.KING) return false;

        return Math.abs(toCol - fromCol) == 2 &&
                board[toRow][toCol] == null;
    }

    public boolean isRightSideCastle(int fromRow, int fromCol, int toRow, int toCol) {
        return isCastleMove(fromRow, fromCol, toRow, toCol) && (toCol - fromCol) == 2;
    }

    // Check detection
    public boolean isChecked(int targetRow, int targetCol) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board[row][col];
                if (piece != null && piece.getColor() == currentColor) {
                    ArrayList<Move> possibleMoves = piece.getPossibleMoves(board);
                    for (Move move : possibleMoves) {
                        if (move.getEndCol() == targetCol && move.getEndRow() == targetRow) {
                            System.out.println("CHECK FOUND");
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean isAttacked(int targetRow, int targetCol, int color) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board[row][col];
                if (piece != null && piece.getColor() != color && piece.getType() != PieceType.PAWN) {

                    ArrayList<Move> possibleMoves = piece.getPossibleMoves(board);
                    for (Move move : possibleMoves) {
                        if (move.getEndCol() == targetCol && move.getEndRow() == targetRow) {
                            return true;
                        }
                    }

                } else if (piece != null && piece.getType() == PieceType.PAWN && piece.getColor() != color) {

                    System.out.println("PAWN CHECK");
                    ArrayList<int[]> attacks = piece.getPawnAttacks(board);
                    for (int[] move : attacks) {
                        if (move[0] == targetRow && move[1] == targetCol) {
                            System.out.println("Attacked " + move[0] + " " + move[1]);
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

        int opponentColor = color == 0 ? 1 : 0;
        int[] kingPos = findKing(opponentColor);
        if (kingPos == null) return false;

        int kingRow = kingPos[0];
        int kingCol = kingPos[1];


        return isChecked(kingRow, kingCol);
    }

    public boolean checkMate(int color) {
        int opponentColor = color == 0 ? 1 : 0;
        int[] kingPos = findKing(opponentColor);
        if (kingPos == null) return false;

        int kingRow = kingPos[0];
        int kingCol = kingPos[1];

        Piece king = board[kingRow][kingCol];
        ArrayList<Move> moves = king.getPossibleMoves(board);
        moves.removeIf(move -> isAttacked(move.getEndRow(), move.getEndCol(), color));

        return gameState == GameState.CHECK && moves.isEmpty();
    }

    // KING CASTLING CHECKS
    private boolean canCastleKingside(int kingRow, int kingCol, int color) {
        if (board[kingRow][kingCol + 1] != null && board[kingRow][kingCol + 2] != null) return false;

        if (isAttacked(kingRow, kingCol + 1, color) ||
                isAttacked(kingRow, kingCol + 2, color)) return false;

        Piece rook = board[kingRow][7];
        return rook != null && rook.getType() == PieceType.ROOK && rook.getColor() == color;
    }

    private boolean canCastleQueenside(int kingRow, int kingCol, int playercolor) {
        if (board[kingRow][kingCol - 1] != null &&
                board[kingRow][kingCol - 2] != null &&
                board[kingRow][kingCol - 3] != null) return false;

        if (isAttacked(kingRow, kingCol + 1, playercolor) ||
                isAttacked(kingRow, kingCol + 2, playercolor)) return false;

        Piece rook = board[kingRow][0];
        return rook != null && rook.getType() == PieceType.ROOK && rook.getColor() == playercolor;
    }

    private boolean hasKingMoved(int playerColor) {
        return (playerColor == 0) ? whiteKingMoved : blackKingMoved;
    }

    private boolean hasRookMoved(int playerColor, boolean rightSide) {
        if (playerColor == 0) {  // White
            return rightSide ? whiteKingsideRookMoved : whiteQueensideRookMoved;
        } else {  // Black
            return rightSide ? blackKingsideRookMoved : blackQueensideRookMoved;
        }
    }

    private void updateCastlingFlags(Piece piece, int fromRow, int fromCol) {
        if (piece.getType() == PieceType.KING) {
            if (piece.getColor() == 0) {  // White
                whiteKingMoved = true;
            } else {  // Black
                blackKingMoved = true;
            }
        }

        if (piece.getType() == PieceType.ROOK) {
            if (piece.getColor() == 0) {  // White rooks
                if (fromRow == 7 && fromCol == 0) {  // Queenside rook
                    whiteQueensideRookMoved = true;
                } else if (fromRow == 7 && fromCol == 7) {  // Kingside rook
                    whiteKingsideRookMoved = true;
                }
            } else {  // Black rooks
                if (fromRow == 0 && fromCol == 0) {  // Queenside rook
                    blackQueensideRookMoved = true;
                } else if (fromRow == 0 && fromCol == 7) {  // Kingside rook
                    blackKingsideRookMoved = true;
                }
            }
        }
    }

    public ArrayList<Move> getCastlingMoves(int kingRow, int kingCol, int playerColor) {
        ArrayList<Move> castlingMoves = new ArrayList<>();

        if (isChecked(kingRow, kingCol)) {
            return castlingMoves;
        }

        if (!hasKingMoved(playerColor) && !hasRookMoved(playerColor, true) && canCastleKingside(kingRow, kingCol, playerColor)) {
            castlingMoves.add(new Move(kingRow, kingCol, kingRow, kingCol + 2, "CASTLERIGHT", null, PieceType.KING, playerColor));
            System.out.println("FOUND CASTLE RIGHT");
        }

        if (!hasKingMoved(playerColor) && !hasRookMoved(playerColor, false) && canCastleQueenside(kingRow, kingCol, playerColor)) {
            castlingMoves.add(new Move(kingRow, kingCol, kingRow, kingCol - 2, "CASTLELEFT", null, PieceType.KING, playerColor));
            System.out.println("FOUND CASTLE LEFT");
        }

        return castlingMoves;
    }

    // Getters
    public Piece getPiece(int row, int col) {
        return board[row][col];
    }

    public int getCurrentColor() {
        return currentColor;
    }

    public int getOpponentColor() {
        return currentColor == 0 ? 1 : 0;
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
//        board[5][5] = new KNIGHT(1, 5, 5, whiteOnBottom);
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