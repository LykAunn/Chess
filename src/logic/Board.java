package logic;

import Pieces.*;
import logic.ai.AI;
import main.Sound;

import java.util.ArrayList;

public class Board {

    // CONSTANTS
    public final int WHITE = 0;
    public final int BLACK = 1;
    public final int BOARD_SIZE = 8;
    public final int CASTLING_KING_MOVE_DISTANCE = 2;
    public final int CASTING_QUEEN_MOVE_DISTANCE = 3;
    public final int FIRST_ROW = 0;
    public final int LAST_ROW = 7;

    private GameObserver observer;
    public static Piece[][] board;
    public static boolean[][] whiteAtkMap;
    public static boolean[][] blackAtkMap;
    private int currentColor = 0;
    private ArrayList<Move> moveHistory;
    private static Move lastMove;
    private GameState gameState = GameState.TITLE;
    public boolean whiteOnBottom;
    Sound sound = new Sound();
    public AI ai;
    public boolean popUpShown = false;

    // Flags to track if pieces have moved
    private boolean whiteKingMoved = false;
    private boolean blackKingMoved = false;
    private boolean whiteKingsideRookMoved = false;
    private boolean whiteQueensideRookMoved = false;
    private boolean blackKingsideRookMoved = false;
    private boolean blackQueensideRookMoved = false;
    private boolean whiteChecked = false;
    private boolean blackChecked = false;
    private String promotedPiece = "";

    public Board() {
        board = new Piece[BOARD_SIZE][BOARD_SIZE];
        whiteAtkMap = new boolean[BOARD_SIZE][BOARD_SIZE];
        blackAtkMap = new boolean[BOARD_SIZE][BOARD_SIZE];
        moveHistory = new ArrayList<>();
        ai = new AI(this, BLACK);
    }

    public void startGame() {
        initializeBoard();
    }

    public void playSE(int i) {
        sound.play(i);
    }

    public void playRandomSE(int i, int j) {

        double random = Math.random();
        if (random < 0.5) {
            playSE(i);
        } else {
            playSE(j);
        }
    }

    public void setObserver(GameObserver observer) {
        this.observer = observer;
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

    public void executeMove(int fromRow, int fromCol, int toRow, int toCol) {

        if (!isValidMove(fromRow, fromCol, toRow, toCol)) {
            return;
        }

        Piece piece = board[fromRow][fromCol];
        Piece capturedPiece = board[toRow][toCol];
        String typeOfMove = null;

        // EN PASSANT
        if(isEnPassantMove(fromRow, fromCol, toRow, toCol)) {
            capturedPiece = handleEnPassant(fromRow, toCol);
            typeOfMove = "EnPassant";
        }

        // CASTLING
        if (isCastleMove(fromRow, fromCol, toRow, toCol)) {
            handleCastling(isKingSideCastle(fromCol, toCol));
            typeOfMove = determineCastlingType(isKingSideCastle(fromCol, toCol));
        }

        // Execute the move
        executeBasicMove(fromRow,fromCol,toRow,toCol, piece);
        PieceType capturedType = capturedPiece == null ? null : capturedPiece.getType();

        // Update castling booleans
        updateCastlingFlags(piece, fromRow, fromCol);

        updateAtkMap(currentColor);

        if (pawnAbleToPromote(toRow, toCol, currentColor)) {
            observer.onPawnPromotion(toRow, toCol, currentColor);
            lastMove = new Move(fromRow, fromCol, toRow, toCol, " ", capturedType, piece.getType(), currentColor);
            return;
        }

        recordMove(fromRow, fromCol, toRow, toCol, piece, typeOfMove, capturedType);

        playMoveSound(isCastleMove(fromRow, fromCol, toRow, toCol), isEnPassantMove(fromRow, fromCol, toRow, toCol),
                capturedPiece != null);

        //Switch turns
        changeColor();

        Move bestMove = ai.findBestMove(3);
        System.out.println("Best Move" + bestMove);

        // Check game state for new current player
        updateGameState();

        //Notify Observers about the move
        notifyObservers(capturedPiece);

    }

    public void recordMove(int fromRow, int fromCol, int toRow, int toCol, Piece piece, String typeOfMove, PieceType capturedType) {
        //Record move into Move
        lastMove = new Move(fromRow, fromCol, toRow, toCol, typeOfMove, capturedType, piece.getType(), currentColor);
        moveHistory.add(lastMove);
    }

    public void executeBasicMove(int fromRow, int fromCol, int toRow, int toCol, Piece piece) {
        board[fromRow][fromCol] = null;
        board[toRow][toCol] = piece;
        piece.row = toRow;
        piece.col = toCol;
    }

    public void updateGameState() {
        if (isInCheck(currentColor)) {
            System.out.println("CHECK ON " + (currentColor == WHITE ? "WHITE" : "BLACK") + " move");
            if (checkmate(currentColor)) {
                gameState = GameState.CHECKMATE;
            } else {
                gameState = GameState.CHECK;
            }

        } else if (isStaleMate(currentColor)) {
            gameState = GameState.STALEMATE;
        } else {
            gameState = GameState.PLAYING;
        }
    }

    public void playMoveSound(boolean isCastle, boolean isEnPassant, boolean isCapture) {
        if (gameState == GameState.CHECKMATE) {
            playSE(9);
        } else if (gameState == GameState.CHECK) {
            playRandomSE(7, 8);
        } else if (isCastle) {
            playRandomSE(5, 6);
        } else if (isCapture || isEnPassant) {
            playRandomSE(3, 4);
        } else {
            playRandomSE(1, 2);
        }


    }


    public void notifyObservers(Piece capturedPiece) {
        if (observer != null) {

            if (capturedPiece != null) {
                observer.onPieceCaptured(capturedPiece);
            }

            observer.onMoveExecuted(lastMove);
            observer.onTurnChanged(currentColor);
            observer.onGameStateChanged(gameState);
            observer.onHistoryAdded(lastMove);
            displayBoard();
        }
    }

    public Piece handleEnPassant(int fromRow, int toCol) {
        Piece captured = board[fromRow][toCol];
        board[fromRow][toCol] = null;
        return captured;
    }

    public boolean isKingSideCastle(int fromCol, int toCol) {
        if (whiteOnBottom) {
            // Normal: kingside = moving right (positive direction)
            return toCol > fromCol;
        } else {
            // Flipped: kingside = moving left (negative direction)
            return toCol < fromCol;
        }
    }

    public void handleCastling(boolean isKingsideCastle) {

        // Determine which row the rooks are on
        int whichRow = (currentColor == WHITE && whiteOnBottom) || (currentColor == BLACK && !whiteOnBottom) ? LAST_ROW : FIRST_ROW;

        if (isKingsideCastle) {
            // KINGSIDE CASTLE
            int rookStartCol, rookEndCol;

            if (whiteOnBottom) {
                // Normal orientation: kingside rook at column 7, moves to column 5
                rookStartCol = LAST_ROW;
                rookEndCol = 5;
            } else {
                // Flipped orientation: kingside rook at column 0, moves to column 2
                rookStartCol = FIRST_ROW;
                rookEndCol = 2;
            }

            Piece castlePiece = board[whichRow][rookStartCol];
            board[whichRow][rookStartCol] = null;
            board[whichRow][rookEndCol] = castlePiece;

        } else {
            // QUEENSIDE CASTLE
            int rookStartCol, rookEndCol;

            if (whiteOnBottom) {
                // Normal orientation: queenside rook at column 0, moves to column 3
                rookStartCol = FIRST_ROW;
                rookEndCol = 3;
            } else {
                // Flipped orientation: queenside rook at column 7, moves to column 5
                rookStartCol = LAST_ROW;
                rookEndCol = 5;
            }

            Piece castlePiece = board[whichRow][rookStartCol];
            board[whichRow][rookStartCol] = null;
            board[whichRow][rookEndCol] = castlePiece;

        }
    }

    public String determineCastlingType(boolean isKingsideCastle) {
        if (isKingsideCastle) {
            return "CASTLEKINGSIDE";
        } else {
            return "CASTLEQUEENSIDE";
        }
    }

    public void clearSelection() {
        if (observer != null) {
            observer.onSelectionCleared();
        }
    }

    public void promotePawn(int row, int col, PieceType selectedPiece, int color) {
        Piece newPiece;
        if (selectedPiece == PieceType.BISHOP) {
            newPiece = new BISHOP(color, row, col, whiteOnBottom);
            promotedPiece = "PROMOTEBISHOP";
        } else if (selectedPiece == PieceType.QUEEN) {
            newPiece = new QUEEN(color, row, col, whiteOnBottom);
            promotedPiece = "PROMOTEQUEEN";
        } else if (selectedPiece == PieceType.ROOK) {
            newPiece = new ROOK(color, row, col, whiteOnBottom);
            promotedPiece = "PROMOTEROOK";
        } else {
            newPiece = new KNIGHT(color, row, col, whiteOnBottom);
            promotedPiece = "PROMOTEKNIGHT";
        }

        lastMove = new Move(lastMove.getStartRow(), lastMove.getStartCol(), row, col, promotedPiece, lastMove.getPieceCaptured(), lastMove.getPieceMoved(), lastMove.getPieceColor());
        observer.onHistoryAdded(lastMove);

        board[row][col] = newPiece;
        popUpShown = false;
        changeColor();

    }

    public void changeColor() {
        currentColor = currentColor == WHITE ? BLACK : WHITE;
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

    public ArrayList<Move> getLegalMoves(Piece piece) {
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

        return (Math.abs(toCol - fromCol) == CASTLING_KING_MOVE_DISTANCE) &&
                board[toRow][toCol] == null;
    }

    public boolean pawnAbleToPromote(int row, int col, int colorToPromote) {
        if (board[row][col] == null) return false;
        if (board[row][col].getColor() != colorToPromote) return false;
        int rowToPromote = (colorToPromote == WHITE && whiteOnBottom) || (colorToPromote == BLACK && !whiteOnBottom) ? FIRST_ROW : LAST_ROW;

        return board[row][col].getType() == PieceType.PAWN && row == rowToPromote;
    }

    // Check detection
    public boolean isAttacked(int targetRow, int targetCol, int defendingColor) {
        int attackingColor = getOtherColor(defendingColor);

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
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
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Piece piece = board[row][col];
                if (piece != null && piece.getColor() == color && piece.getType() == PieceType.KING) {
                    return new int[]{row, col};
                }
            }
        }
        return null;
    }

    public int returnOpponentColor(int color) {
        return color == WHITE ? BLACK : WHITE;
    }

    public boolean checkmate(int color) {
        int opponentColor = returnOpponentColor(color);

        // Must be in check
        if (!isInCheck(color)) {
            return false;
        }

        // Check for legal moves
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
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

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
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

        int rookKingSide = whiteOnBottom ? LAST_ROW : FIRST_ROW;
        Piece rook = board[kingRow][rookKingSide];
        return rook != null && rook.getType() == PieceType.ROOK && rook.getColor() == color;
    }

    private boolean canCastleQueenside(int kingRow, int kingCol, int playercolor) {
        int direction = whiteOnBottom ? BLACK : -1;
        if (board[kingRow][kingCol - direction] != null &&
                board[kingRow][kingCol - (direction * 2)] != null &&
                board[kingRow][kingCol - (direction * 3)] != null) return false;

        if (isAttacked(kingRow, kingCol + direction, playercolor) ||
                isAttacked(kingRow, kingCol + (direction * 2), playercolor)) return false;

    int rookQueenSide = whiteOnBottom ? FIRST_ROW : LAST_ROW;
        Piece rook = board[kingRow][rookQueenSide];
        return rook != null && rook.getType() == PieceType.ROOK && rook.getColor() == playercolor;
    }

    private boolean hasKingMoved(int playerColor) {
        return (playerColor == WHITE) ? whiteKingMoved : blackKingMoved;
    }

    private boolean hasRookMoved(int playerColor, boolean rightSide) {
        if (playerColor == WHITE) {
            return rightSide ? whiteKingsideRookMoved : whiteQueensideRookMoved;
        } else {  // Black
            return rightSide ? blackKingsideRookMoved : blackQueensideRookMoved;
        }
    }

    private void updateCastlingFlags(Piece piece, int fromRow, int fromCol) {
        if (piece.getType() == PieceType.KING) {
            if (piece.getColor() == WHITE) {  // White
                whiteKingMoved = true;
            } else {  // Black
                blackKingMoved = true;
            }
        }

        if (piece.getType() == PieceType.ROOK) {
            if (piece.getColor() == WHITE) {  // White rooks
                int whiteBackRank = whiteOnBottom ? LAST_ROW : FIRST_ROW;

                if (fromRow == whiteBackRank && fromCol == FIRST_ROW) {  // Queenside rook
                    whiteQueensideRookMoved = true;
                } else if (fromRow == whiteBackRank && fromCol == LAST_ROW) {  // Kingside rook
                    whiteKingsideRookMoved = true;

                }
            } else {  // Black rooks
                int blackBackRank = whiteOnBottom ? FIRST_ROW : LAST_ROW;
                if (fromRow == blackBackRank && fromCol == FIRST_ROW) {  // Queenside rook
                    blackQueensideRookMoved = true;
                } else if (fromRow == blackBackRank && fromCol == LAST_ROW) {  // Kingside rook
                    blackKingsideRookMoved = true;
                }
            }
        }
    }

    public ArrayList<Move> getCastlingMoves(int kingRow, int kingCol, int playerColor) {
        ArrayList<Move> castlingMoves = new ArrayList<>();
        int direction = whiteOnBottom ? BLACK : -1;

        // Unable to castle in check
        if (isAttacked(kingRow, kingCol, playerColor)) {
            return castlingMoves;
        }

        if (!hasKingMoved(playerColor) && !hasRookMoved(playerColor, true)) {

            if (canCastleKingside(kingRow, kingCol, playerColor)) {

                if (!isAttacked(kingRow, kingCol + direction, playerColor) && !isAttacked(kingRow, kingCol + (direction * CASTLING_KING_MOVE_DISTANCE), playerColor)) {
                    castlingMoves.add(new Move(kingRow, kingCol, kingRow, kingCol + (direction * CASTLING_KING_MOVE_DISTANCE), "CASTLERIGHT", null, PieceType.KING, playerColor));
                    System.out.println("FOUND CASTLE RIGHT");
                }
            }

        }

        if (!hasKingMoved(playerColor) && !hasRookMoved(playerColor, false) && canCastleQueenside(kingRow, kingCol, playerColor)) {
            if (!canCastleKingside(kingRow, kingCol, playerColor)) {

                if (!isAttacked(kingRow, kingCol - direction, playerColor) && !isAttacked(kingRow, kingCol - (direction * CASTLING_KING_MOVE_DISTANCE), playerColor)) {
                    castlingMoves.add(new Move(kingRow, kingCol, kingRow, kingCol - (direction * CASTLING_KING_MOVE_DISTANCE), "CASTLERIGHT", null, PieceType.KING, playerColor));
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

    public void updateAtkMap(int currentColor) { //Squares currentColor attacks
        clearAtkMap(currentColor);
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Piece currentPiece = board[row][col];

                if (currentPiece != null && currentPiece.getColor() == currentColor) {
                    addPieceAttacks(currentPiece, currentColor);
                }
            }
        }
    }

    private void addPieceAttacks(Piece piece, int color) {
        ArrayList<Move> attackedSquares = piece.getPossibleMoves(board);

        for (Move move : attackedSquares) {
            markSquareAsAttacked(move.getEndRow(), move.getEndCol(), color);
        }
    }

    private void markSquareAsAttacked(int row, int col, int color) {
        if (color == BLACK) {
            blackAtkMap[row][col] = true;
        } else {
            whiteAtkMap[row][col] = true;
        }
    }

    public void clearAtkMap(int currentColor) {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (currentColor == BLACK) {
                    blackAtkMap[i][j] = false;
                } else {
                    whiteAtkMap[i][j] = false;
                }
            }
        }
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

    public int getOtherColor(int color) {
        return color == BLACK ? WHITE : BLACK;
    }

    public static Move getLastMove() {
        return lastMove;
    }

    public GameState getGameState() {
        return gameState;
    }

    public void initializeBoard() {

        // Row positions depend on orientation
        int whiteRow1 = whiteOnBottom ? LAST_ROW : FIRST_ROW; // main pieces
        int whiteRow2 = whiteOnBottom ? 6 : 1; // pawns
        int blackRow1 = whiteOnBottom ? FIRST_ROW : LAST_ROW; // main pieces
        int blackRow2 = whiteOnBottom ? 1 : 6; // pawns

        // Column positions for king/queen
        int whiteQueenCol = whiteOnBottom ? 3 : 4;
        int whiteKingCol  = whiteOnBottom ? 4 : 3;
        int blackQueenCol = whiteOnBottom ? 3 : 4;
        int blackKingCol  = whiteOnBottom ? 4 : 3;

        // Black pieces (top side depending on orientation)
        board[blackRow1][0] = new ROOK(BLACK, blackRow1, 0, whiteOnBottom);
        board[blackRow1][1] = new KNIGHT(BLACK, blackRow1, 1, whiteOnBottom);
        board[blackRow1][2] = new BISHOP(BLACK, blackRow1, 2, whiteOnBottom);
        board[blackRow1][blackQueenCol] = new QUEEN(BLACK, blackRow1, blackQueenCol, whiteOnBottom);
        board[blackRow1][blackKingCol]  = new KING(BLACK, blackRow1, blackKingCol, whiteOnBottom);
        board[blackRow1][5] = new BISHOP(BLACK, blackRow1, 5, whiteOnBottom);
        board[blackRow1][6] = new KNIGHT(BLACK, blackRow1, 6, whiteOnBottom);
        board[blackRow1][7] = new ROOK(BLACK, blackRow1, 7, whiteOnBottom);

        for (int i = 0; i < 8; i++) {
            board[blackRow2][i] = new PAWN(BLACK, blackRow2, i, whiteOnBottom);
        }

        // White pieces (bottom side depending on orientation)
        board[whiteRow1][0] = new ROOK(WHITE, whiteRow1, 0, whiteOnBottom);
        board[whiteRow1][1] = new KNIGHT(WHITE, whiteRow1, 1, whiteOnBottom);
        board[whiteRow1][2] = new BISHOP(WHITE, whiteRow1, 2, whiteOnBottom);
        board[whiteRow1][whiteQueenCol] = new QUEEN(WHITE, whiteRow1, whiteQueenCol, whiteOnBottom);
        board[whiteRow1][whiteKingCol]  = new KING(WHITE, whiteRow1, whiteKingCol, whiteOnBottom);
        board[whiteRow1][5] = new BISHOP(WHITE, whiteRow1, 5, whiteOnBottom);
        board[whiteRow1][6] = new KNIGHT(WHITE, whiteRow1, 6, whiteOnBottom);
        board[whiteRow1][7] = new ROOK(WHITE, whiteRow1, 7, whiteOnBottom);

        for (int i = 0; i < BOARD_SIZE; i++) {
            board[whiteRow2][i] = new PAWN(WHITE, whiteRow2, i, whiteOnBottom);
        }

        playSE(0);
    }

    public void displayBoard() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
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