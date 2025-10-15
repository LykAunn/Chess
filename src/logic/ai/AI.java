package logic.ai;

import Pieces.*;
import logic.Board;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class AI {
    final static int pawnValue = 100;
    final static int knightValue = 300;
    final static int bishopValue = 300;
    final static int rookValue = 500;
    final static int queenValue = 900;
    final static int BOARD_SIZE = 7;

    static Board board;
    private int aiColor;

    public AI(Board board, int aiColor) {
        AI.board = board;
        this.aiColor = aiColor;
    }

    public static int Evaluate() {
        int whiteEval = CountMaterial(board.WHITE);
        int blackEval = CountMaterial(board.BLACK);

        int evaluation = whiteEval - blackEval;

        int perspective = (board.getCurrentColor() == board.WHITE) ? 1 : -1;
        return evaluation * perspective;
    }

    private static int CountMaterial(int colourIndex) {
        int material = 0;

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {

                Piece piece = board.getPiece(i, j);

                if (piece != null && piece.getColor() == colourIndex) {
                    material += getPieceValue(piece.getType());
                }
            }
        }

        return material;
    }

    private static int getPieceValue(PieceType type) {
        return switch (type) {
            case PAWN -> pawnValue;
            case KNIGHT -> knightValue;
            case BISHOP -> bishopValue;
            case ROOK -> rookValue;
            case QUEEN -> queenValue;
            default -> 0;
        };
    }

    private ArrayList<Move> getPossibleMoves(int color) {
        ArrayList<Move> allMoves = new ArrayList<>();

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                Piece piece = board.getPiece(i, j);

                if (piece != null && piece.getColor() == color) {
                    ArrayList<Move> pieceMoves = board.getLegalMoves(piece);
                    allMoves.addAll(pieceMoves);
                }
            }
        }

        return allMoves;
    }

    private int evaluateMove(Move move) {
        // Simulate the move
        Piece piece = board.getPiece(move.getStartRow(), move.getStartCol());
        Piece capturedPiece = board.getPiece(move.getEndRow(), move.getEndCol());

        // Make temp move
        board.executeBasicMove(move.getStartRow(), move.getStartCol(), move.getEndRow(), move.getEndCol(),piece);

        // Evaluate Position
        int score = Evaluate();

        // Undo move
        board.executeBasicMove(move.getEndRow(), move.getEndCol(), move.getStartRow(), move.getStartCol(),piece);
        board.board[move.getEndRow()][move.getEndCol()] = capturedPiece;

        return score;
    }

    public int Search(int depth) {
        if (depth == 0) {
            return Evaluate();
        }

        ArrayList<Move> moves = getPossibleMoves(board.getCurrentColor());
        if (moves.isEmpty()) {
            if (board.isInCheck(board.getCurrentColor())) {
                return -9999;
            }
            return 0; // Stalemate = draw
        }

        int bestEvaluation = Integer.MIN_VALUE;

        for (Move move : moves) {
            // Simulate the move
            Piece piece = board.getPiece(move.getStartRow(), move.getStartCol());
            Piece capturedPiece = board.getPiece(move.getEndRow(), move.getEndCol());

            // SAVE original positions
            int originalRow = piece.row;
            int originalCol = piece.col;

            // Make temp move
            board.executeBasicMove(move.getStartRow(), move.getStartCol(), move.getEndRow(), move.getEndCol(),piece);
            board.changeColor();

            // Recursively search (opponents turn, so negate)
            int evaluation = - Search(depth - 1);
            bestEvaluation = Math.max(bestEvaluation, evaluation);

            board.changeColor();

            // Undo move - RESTORE EVERYTHING
            board.board[move.getStartRow()][move.getStartCol()] = piece;
            board.board[move.getEndRow()][move.getEndCol()] = capturedPiece;
            piece.row = originalRow;  // CRITICAL: Restore piece position
            piece.col = originalCol;
        }

        return bestEvaluation;
    }

    public Move findBestMove(int depth) {
        ArrayList<Move> moves = getPossibleMoves(aiColor);

        if (moves.isEmpty()) {
            return null;
        }

        Move bestMove = null;
        int bestEvaluation = Integer.MIN_VALUE;

        for (Move move : moves) {
            // Simulate the move
            Piece piece = board.getPiece(move.getStartRow(), move.getStartCol());
            Piece capturedPiece = board.getPiece(move.getEndRow(), move.getEndCol());

            // SAVE original positions
            int originalRow = piece.row;
            int originalCol = piece.col;

            // Make temp move
            board.executeBasicMove(move.getStartRow(), move.getStartCol(),
                    move.getEndRow(), move.getEndCol(), piece);
            board.changeColor(); // IMPORTANT: Switch turns

            // Evaluate this move
            int evaluation = -Search(depth - 1);

            // Undo move - RESTORE EVERYTHING
            board.changeColor();
            board.board[originalRow][originalCol] = piece;
            board.board[move.getEndRow()][move.getEndCol()] = capturedPiece;
            piece.row = originalRow;
            piece.col = originalCol;


            // Track best move
            if (evaluation > bestEvaluation) {
                bestEvaluation = evaluation;
                bestMove = move;
            }
        }

        System.out.println("Best move evaluation: " + bestEvaluation);
        return bestMove;
    }
}
