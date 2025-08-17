package Pieces;

import java.util.ArrayList;

public class BISHOP extends Piece {

    public BISHOP(int color, int row, int col, boolean whiteOnBottom) {
        super(color, PieceType.BISHOP, row, col, whiteOnBottom);
    }

    public ArrayList<Move> getPossibleMoves(Piece[][] board) {
        ArrayList<Move> moves = new ArrayList<>();

        // Define all 4 directions: TopLeft, TopRight, BottomLeft, BottomRight
        int[][] directions = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};

        moves = getSlidingMoves(board, directions);

        return moves;
    }
}
