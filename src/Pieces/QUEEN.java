package Pieces;

import java.util.ArrayList;

public class QUEEN extends Piece {

    public QUEEN(int color, int row, int col, boolean whiteOnBottom) {
        super(color, PieceType.QUEEN, row, col, whiteOnBottom);
    }

    public ArrayList<Move> getPossibleMoves(Piece[][] board) {
        ArrayList<Move> moves = new ArrayList<>();

        int[][] directions = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}, {-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        moves = getSlidingMoves(board, directions);
        
        return moves;

    }
}
