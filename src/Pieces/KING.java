package Pieces;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class KING extends Piece {

    public KING(int color, int row, int col, boolean whiteOnBottom) {
        super(color, PieceType.KING, row, col, whiteOnBottom);
    }

    public ArrayList<Move> getPossibleMoves(Piece[][] board) {
        ArrayList<Move> moves = new ArrayList<>();

        //Define all directions: Up, Down, Left, Right
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        moves = getNonSlidingMoves(board, directions, row, col);
        return moves;
    }
}
