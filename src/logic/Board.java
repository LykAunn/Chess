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
    private GameState gameState = GameState.TITLE;
    public boolean whiteOnBottom;
    Sound sound = new Sound();

    // Flags to track if pieces have moved
    private boolean whiteKingMoved = false;
    private boolean blackKingMoved = false;
    private boolean whiteKingsideRookMoved = false;
    private boolean whiteQueensideRookMoved = false;
    private boolean blackKingsideRookMoved = false;
    private boolean blackQueensideRookMoved = false;
    private boolean whiteChecked = false;
    private boolean blackChecked = false;

    public Board() {
        board = new Piece[8][8];
        moveHistory = new ArrayList<>();
        playSE(0);
    }

    public void startGame() {
        initializeBoard();
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

        System.out.println(currentColor);
        Piece piece = board[row][col];
        if (piece != null && piece.getColor() == currentColor) {
            ArrayList<Move> legalMoves = getLegalMoves(piece);

            if (observer != null) {
                observer.onPieceSelected(row, col, legalMoves);
            }
        }
    }

    public boolean executeMove(int fromRow, int fromCol, int toRow, int toCol) {

        if (!isValidMove(fromRow, fromCol, toRow, toCol)) {
            return false;
        }

        Piece piece = board[fromRow][fromCol];
        Piece capturedPiece = board[toRow][toCol];

//        if (piece != null && piece.getType() ==  PieceType.PAWN) {
//            ArrayList<int[]> test = piece.getPawnAttacks(board);
//        }

        String typeOfMove = null;

        //En Passant
        if (isEnPassantMove(fromRow, fromCol, toRow, toCol)) {
            capturedPiece = board[fromRow][toCol];
            board[fromRow][toCol] = null;
            typeOfMove = "ENPASSANT";
        }

        if (isCastleMove(fromRow, fromCol, toRow, toCol)) {
            System.out.println("Current Color = " + currentColor + " WhiteOnBottom = " + whiteOnBottom);

            // Determine which row the rooks are on
            int whichRow = (currentColor == 0 && whiteOnBottom) || (currentColor == 1 && !whiteOnBottom) ? 7 : 0;

            // Determine if this is kingside or queenside based on king movement direction
            boolean isKingsideCastle;
            if (whiteOnBottom) {
                // Normal: kingside = moving right (positive direction)
                isKingsideCastle = toCol > fromCol;
            } else {
                // Flipped: kingside = moving left (negative direction)
                isKingsideCastle = toCol < fromCol;
            }

            if (isKingsideCastle) {
                // KINGSIDE CASTLE
                int rookStartCol, rookEndCol;

                if (whiteOnBottom) {
                    // Normal orientation: kingside rook at column 7, moves to column 5
                    rookStartCol = 7;
                    rookEndCol = 5;
                } else {
                    // Flipped orientation: kingside rook at column 0, moves to column 2
                    rookStartCol = 0;
                    rookEndCol = 2;
                }

                Piece castlePiece = board[whichRow][rookStartCol];
                board[whichRow][rookStartCol] = null;
                board[whichRow][rookEndCol] = castlePiece;
                System.out.println("Moving kingside rook from col " + rookStartCol + " to col " + rookEndCol);

                typeOfMove = "CASTLEKINGSIDE";
            } else {
                // QUEENSIDE CASTLE
                int rookStartCol, rookEndCol;

                if (whiteOnBottom) {
                    // Normal orientation: queenside rook at column 0, moves to column 3
                    rookStartCol = 0;
                    rookEndCol = 3;
                } else {
                    // Flipped orientation: queenside rook at column 7, moves to column 5
                    rookStartCol = 7;
                    rookEndCol = 5;
                }

                Piece castlePiece = board[whichRow][rookStartCol];
                board[whichRow][rookStartCol] = null;
                board[whichRow][rookEndCol] = castlePiece;

                typeOfMove = "CASTLEQUEENSIDE";
            }
        }

        // Execute the move
        board[fromRow][fromCol] = null;
        board[toRow][toCol] = piece;
        piece.row = toRow;
        piece.col = toCol;

        //Record move into Move
        PieceType capturedType = capturedPiece == null ? null : capturedPiece.getType();
        lastMove = new Move(fromRow, fromCol, toRow, toCol, typeOfMove, capturedType, piece.getType(), currentColor);
        moveHistory.add(lastMove);

        // Update castling booleans
        updateCastlingFlags(piece, fromRow, fromCol);

        //Switch turns
        currentColor = currentColor == 0 ? 1 : 0;

        // Check game state for new current player
        if (isInCheck(currentColor)) {
            System.out.println("CHECK ON " + (currentColor == 0 ? "WHITE" : "BLACK") + " move");
            if (checkmate(currentColor == 0 ? 1 : 0)) {
                gameState = GameState.CHECKMATE;
                playSE(9);
            } else {
                gameState = GameState.CHECK;
                playRandomSE(7, 8);
            }

        } else if (isStaleMate(currentColor)) {
            gameState = GameState.STALEMATE;
        } else {
            gameState = GameState.PLAYING;
        }

        if (isCastleMove(fromRow, fromCol, toRow, toCol)) {
            playRandomSE(5, 6);
        } else if (capturedPiece != null || isEnPassantMove(fromRow, fromCol, toRow, toCol)) {
            playRandomSE(3, 4);
        } else {
            playRandomSE(1, 2);
        }


        //Notify Observers about the move
        if (observer != null) {
            observer.onMoveExecuted(lastMove);
            observer.onTurnChanged(currentColor);
            observer.onGameStateChanged(gameState);
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

        ArrayList<Move> legalMoves = getLegalMoves(piece);

        for (Move move : legalMoves) {
            if (move.getEndCol() == toCol && move.getEndRow() == toRow) {
                return true;
            }
        }
        return false;
    }

    public boolean isMoveLegal(int fromRow, int fromCol, int toRow, int toCol) {
        Piece piece = board[fromRow][fromCol];
        if (piece == null) return false;

        Piece capturedPiece = board[toRow][toCol];

        // Temporary Move
        board[toRow][toCol] = piece;
        board[fromRow][fromCol] = null;
        piece.row = toRow;
        piece.col = toCol;

        int[] kingPos = findKing(piece.getColor());
        boolean isLegal = true;

        if (kingPos != null) {
            isLegal = !isAttacked(kingPos[0], kingPos[1], piece.getColor());
        }

        // Restore original board
        board[fromRow][fromCol] = piece;
        board[toRow][toCol] = capturedPiece;
        piece.row = fromRow;
        piece.col = fromCol;

        return isLegal;
    }

    private ArrayList<Move> getLegalMoves(Piece piece) {
        ArrayList<Move> allMoves = piece.getPossibleMoves(board);
        ArrayList<Move> legalMoves = new ArrayList<>();

        for (Move move : allMoves) {
            if (isMoveLegal(move.getStartRow(), move.getStartCol(), move.getEndRow(), move.getEndCol())) {
                legalMoves.add(move);
            }
        }

        // Add Castling Move for king
        if (piece.getType() ==  PieceType.KING) {
            ArrayList<Move> castleMoves = getCastlingMoves(piece.getRow(), piece.getCol(), piece.getColor());
            legalMoves.addAll(castleMoves);
        }

        return legalMoves;
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

        return (Math.abs(toCol - fromCol) == 2) &&
                board[toRow][toCol] == null;
    }

    public boolean isRightSideCastle(int fromRow, int fromCol, int toRow, int toCol) {
        return isCastleMove(fromRow, fromCol, toRow, toCol) && (toCol - fromCol) == 2;
    }

    // Check detection
    public boolean isAttacked(int targetRow, int targetCol, int defendingColor) {
        int attackingColor = defendingColor == 0 ? 1 : 0;

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board[row][col];

                if (piece != null && piece.getColor() == attackingColor) {

                    if (piece.getType() == PieceType.PAWN) {
                        // Special case for pawns with special attacking move
                        ArrayList<int[]> attacks = piece.getPawnAttacks(board);
                        for (int[] attack : attacks) {
                            if (attack[0] == targetRow && attack[1] == targetCol) {
                                return true;
                            }
                        }

                    } else {
                        // Check possible moves for other pieces
                        ArrayList<Move> possibleMoves = piece.getPossibleMoves(board);
                        for (Move move : possibleMoves) {
                            if (move.getEndRow() == targetRow && move.getEndCol() == targetCol) {
                                return true;
                            }
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

    public boolean checkmate(int color) {
        int opponentColor = color == 0 ? 1 : 0;

        // Must be in check
        if (isInCheck(color)) {
            return false;

        }

        // Check for legal moves
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board[row][col];
                if (piece != null && piece.getColor() == opponentColor) {
                    ArrayList<Move> legalMoves = getLegalMoves(piece);
                    if (legalMoves.isEmpty()) return false;
                }
            }
        }

        return true; // No legal moves found = Checkmate
    }

    public boolean isInCheck(int color) {
        int[] kingPos = findKing(color);
        if (kingPos == null) return false;

        return isAttacked(kingPos[0], kingPos[1], color);
    }

    // Stalemate detection (Not in check but no legal moves)
    public boolean isStaleMate(int color) {
        if (isInCheck(color)) {
            return false;
        }

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board[row][col];
                if (piece != null && piece.getColor() == color) {
                    ArrayList<Move> legalMoves = getLegalMoves(piece);
                    if (!legalMoves.isEmpty()) return false; // FOUND LEGAL MOVE
                }
            }
        }

        return true; // No legal moves and not in check
    }


    // KING CASTLING CHECKS
    private boolean canCastleKingside(int kingRow, int kingCol, int color) {
        int direction = whiteOnBottom ? 1 : -1;

        if (board[kingRow][kingCol + direction] != null && board[kingRow][kingCol + (direction * 2)] != null) return false;

        if (isAttacked(kingRow, kingCol + direction, color) ||
                isAttacked(kingRow, kingCol + (direction * 2), color)) return false;

        int rookKingSide = whiteOnBottom ? 7 : 0;
        Piece rook = board[kingRow][rookKingSide];
        return rook != null && rook.getType() == PieceType.ROOK && rook.getColor() == color;
    }

    private boolean canCastleQueenside(int kingRow, int kingCol, int playercolor) {
        int direction = whiteOnBottom ? 1 : -1;
        if (board[kingRow][kingCol - direction] != null &&
                board[kingRow][kingCol - (direction * 2)] != null &&
                board[kingRow][kingCol - (direction * 3)] != null) return false;

        if (isAttacked(kingRow, kingCol + direction, playercolor) ||
                isAttacked(kingRow, kingCol + (direction * 2), playercolor)) return false;

    int rookQueenSide = whiteOnBottom ? 0 : 7;
        Piece rook = board[kingRow][rookQueenSide];
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
                int whiteBackRank = whiteOnBottom ? 7 : 0;

                if (fromRow == whiteBackRank && fromCol == 0) {  // Queenside rook
                    whiteQueensideRookMoved = true;
                } else if (fromRow == whiteBackRank && fromCol == 7) {  // Kingside rook
                    whiteKingsideRookMoved = true;

                }
            } else {  // Black rooks
                int blackBackRank = whiteOnBottom ? 0 : 7;
                if (fromRow == blackBackRank && fromCol == 0) {  // Queenside rook
                    blackQueensideRookMoved = true;
                } else if (fromRow == blackBackRank && fromCol == 7) {  // Kingside rook
                    blackKingsideRookMoved = true;
                }
            }
        }
    }

    public ArrayList<Move> getCastlingMoves(int kingRow, int kingCol, int playerColor) {
        ArrayList<Move> castlingMoves = new ArrayList<>();
        int direction = whiteOnBottom ? 1 : -1;

        // Unable to castle in check
        if (isAttacked(kingRow, kingCol, playerColor)) {
            return castlingMoves;
        }

        if (!hasKingMoved(playerColor) && !hasRookMoved(playerColor, true)) {

            if (canCastleKingside(kingRow, kingCol, playerColor)) {

                if (!isAttacked(kingRow, kingCol + direction, playerColor) && !isAttacked(kingRow, kingCol + (direction * 2), playerColor)) {
                    castlingMoves.add(new Move(kingRow, kingCol, kingRow, kingCol + (direction * 2), "CASTLERIGHT", null, PieceType.KING, playerColor));
                    System.out.println("FOUND CASTLE RIGHT");
                }
            }

        }

        if (!hasKingMoved(playerColor) && !hasRookMoved(playerColor, false) && canCastleQueenside(kingRow, kingCol, playerColor)) {
            if (!canCastleKingside(kingRow, kingCol, playerColor)) {

                if (!isAttacked(kingRow, kingCol - direction, playerColor) && !isAttacked(kingRow, kingCol - (direction * 2), playerColor)) {
                    castlingMoves.add(new Move(kingRow, kingCol, kingRow, kingCol - (direction * 2), "CASTLERIGHT", null, PieceType.KING, playerColor));
                    System.out.println("FOUND CASTLE RIGHT");
                }
            }
        }

        return castlingMoves;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public void setPlayerColor(boolean whiteBottom) {
        whiteOnBottom = whiteBottom;
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

    public GameState getGameState() {
        return gameState;
    }

    public void initializeBoard() {
        int whiteColor = 0; // white = 0
        int blackColor = 1; // black = 1

        // Row positions depend on orientation
        int whiteRow1 = whiteOnBottom ? 7 : 0; // main pieces
        int whiteRow2 = whiteOnBottom ? 6 : 1; // pawns
        int blackRow1 = whiteOnBottom ? 0 : 7; // main pieces
        int blackRow2 = whiteOnBottom ? 1 : 6; // pawns

        // Column positions for king/queen
        int whiteQueenCol = whiteOnBottom ? 3 : 4;
        int whiteKingCol  = whiteOnBottom ? 4 : 3;
        int blackQueenCol = whiteOnBottom ? 3 : 4;
        int blackKingCol  = whiteOnBottom ? 4 : 3;

        // Black pieces (top side depending on orientation)
        board[blackRow1][0] = new ROOK(blackColor, blackRow1, 0, whiteOnBottom);
        board[blackRow1][1] = new KNIGHT(blackColor, blackRow1, 1, whiteOnBottom);
        board[blackRow1][2] = new BISHOP(blackColor, blackRow1, 2, whiteOnBottom);
        board[blackRow1][blackQueenCol] = new QUEEN(blackColor, blackRow1, blackQueenCol, whiteOnBottom);
        board[blackRow1][blackKingCol]  = new KING(blackColor, blackRow1, blackKingCol, whiteOnBottom);
        board[blackRow1][5] = new BISHOP(blackColor, blackRow1, 5, whiteOnBottom);
        board[blackRow1][6] = new KNIGHT(blackColor, blackRow1, 6, whiteOnBottom);
        board[blackRow1][7] = new ROOK(blackColor, blackRow1, 7, whiteOnBottom);

        for (int i = 0; i < 8; i++) {
            board[blackRow2][i] = new PAWN(blackColor, blackRow2, i, whiteOnBottom);
        }

        // White pieces (bottom side depending on orientation)
        board[whiteRow1][0] = new ROOK(whiteColor, whiteRow1, 0, whiteOnBottom);
        board[whiteRow1][1] = new KNIGHT(whiteColor, whiteRow1, 1, whiteOnBottom);
        board[whiteRow1][2] = new BISHOP(whiteColor, whiteRow1, 2, whiteOnBottom);
        board[whiteRow1][whiteQueenCol] = new QUEEN(whiteColor, whiteRow1, whiteQueenCol, whiteOnBottom);
        board[whiteRow1][whiteKingCol]  = new KING(whiteColor, whiteRow1, whiteKingCol, whiteOnBottom);
        board[whiteRow1][5] = new BISHOP(whiteColor, whiteRow1, 5, whiteOnBottom);
        board[whiteRow1][6] = new KNIGHT(whiteColor, whiteRow1, 6, whiteOnBottom);
        board[whiteRow1][7] = new ROOK(whiteColor, whiteRow1, 7, whiteOnBottom);

        for (int i = 0; i < 8; i++) {
            board[whiteRow2][i] = new PAWN(whiteColor, whiteRow2, i, whiteOnBottom);
        }
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