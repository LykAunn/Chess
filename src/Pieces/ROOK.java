package Pieces;

import java.util.ArrayList;

public class ROOK extends Piece {

    public ROOK(int color, int row, int col, boolean whiteOnBottom) {
        super(color, PieceType.ROOK, row, col, whiteOnBottom);
    }

    public ArrayList<Move> getPossibleMoves(Piece[][] board) {
        ArrayList<Move> moves = new ArrayList<>();

        // Define all 4 directions: up, down, left, right
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        moves = getSlidingMoves(board, directions);

        return moves;
    }
}
