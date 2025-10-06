package Pieces;

import java.util.ArrayList;

public class KNIGHT extends Piece {

    public KNIGHT(int color, int row, int col, boolean whiteOnBottom) {
        super(color, PieceType.KNIGHT, row, col, whiteOnBottom);
    }

    public ArrayList<Move> getPossibleMoves(Piece[][] board) {
        ArrayList<Move> moves = new ArrayList<>();

        int[][] directions = {{-2, 1}, {-2, -1}, {-1, -2}, {1, -2}, {1, 2}, {-1, 2}, {2, -1}, {2, 1}};

        moves = getNonSlidingMoves(board, directions, row, col);
        return moves;
    }
}