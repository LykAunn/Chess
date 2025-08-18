package Pieces;

import logic.Board;

import java.util.ArrayList;

public class PAWN extends Piece {

    public PAWN(int color, int row, int col, boolean whiteOnBottom) {
        super(color, PieceType.PAWN, row, col, whiteOnBottom);
    }

    public ArrayList<Move> getPossibleMoves(Piece[][] board) {

        ArrayList<Move> moves = new ArrayList<>();
        int direction = (color == 0 && whiteOnBottom) || (color == 1 && !whiteOnBottom) ? -1 : 1;

        //One Square forward
        if (isInBound(board, row + direction, col) && isNotObstructed(board, row + direction, col)) {
            moves.add(new Move(row, col, row + direction, col, null, null, null));
        }

        //Two Squares forward
        if (isInBound(board, row + (direction * 2), col) && isNotObstructed(board, row + (direction * 2), col) && (row == 1 || row == 6)) {
            moves.add(new Move(row, col, row + (direction * 2), col, null, null, null));
        }

        //Diagonal
        if (isInBound(board, row + direction, col + 1) && targetCheck(board, row + direction, col + 1, color)) {
            moves.add(new Move(row, col, row + direction, col + 1, null, null, null));
        }
        if (isInBound(board, row + direction, col - 1) && targetCheck(board, row + direction, col - 1, color)) {
            moves.add(new Move(row, col, row + direction, col - 1, null, null, null));
        }

        //En Passant Check
        if (enPassantCheck(board, row + direction, col + 1)) {
            moves.add(new Move(row, col, row + direction, col + 1, "EnPassant", null, null)); //Right
        }

        if (enPassantCheck(board, row + direction, col - 1)) {
            moves.add(new Move(row, col, row + direction, col - 1, "EnPassant", null, null)); //Left
        }

        return moves;
    }

    public boolean isNotObstructed(Piece[][] board, int newRow, int newCol) {
        boolean check = true;
        if (newRow > row) {
            for (int i = row + 1; i <= newRow; i++) {
                if (board[i][col] != null) {
                    check = false;
                    break;
                }
            }
        } else {
            for (int i = row - 1; i >= newRow; i--) {
                if (board[i][col] != null) {
                    check = false;
                    break;
                }
            }
        }
        return check;
    }

    private boolean enPassantCheck(Piece[][] board, int newRow, int newCol) {
        System.out.println("Checking en passant for destination: " + newRow + "," + newCol);

        // Bounds check
        if (!isInBound(board, newRow, newCol)) {
            return false;
        }

        // Pawn must be at correct rank
        if ((color == 0 && row != 3) || (color == 1 && row != 4)) {
            return false;
        }

        // Adjacent square (same row as your pawn) must have enemy pawn
        Piece adjacentPiece = board[row][newCol];

        if (adjacentPiece == null ||
                adjacentPiece.getType() != PieceType.PAWN ||
                adjacentPiece.getColor() == this.color) {
            return false;
        }

        // Check if that pawn just moved 2 squares
        Move lastMove = Board.getLastMove();

        if (lastMove != null) {
            return lastMove.getEndCol() == newCol &&
                    lastMove.getEndRow() == row &&
                    Math.abs(lastMove.getStartRow() - lastMove.getEndRow()) == 2 &&
                    lastMove.pieceMoved == PieceType.PAWN &&
                    board[newRow][newCol] == null;
        }

        return false;
    }

    public ArrayList<int[]> getPawnAttacks(Piece[][] board) {
        ArrayList<int[]> attacks = new ArrayList<>();
        int direction = (color == 0 && whiteOnBottom) || (color == 1 && !whiteOnBottom) ? -1 : 1;

        int attackRow = row + direction;
        if (attackRow < 8 && attackRow >= 0) {
            if (col - 1 >= 0) {
                attacks.add(new int[]{attackRow, col - 1});
                System.out.println("!!!!!!!! " + attackRow + "!" + (col - 1));
            }

            if (col + 1 < 8) {
                attacks.add(new int[]{attackRow, col + 1});
                System.out.println("!!!!!!!! " + attackRow + "!" + (col + 1));
            }
        }

        return attacks;
    }
}
//        if (color.equals("WHITE") && whiteOnBottom) {
//            if (board[row - 1][col] == null) {
//                moves.add(new Move(row, col, row - 1, col));
//                if (board[row - 2][col] == null && !hasMoved) {
//                    moves.add(new Move(row, col, row - 2, col));
//                }
//            }
//        }