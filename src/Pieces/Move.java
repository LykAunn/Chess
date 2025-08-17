package Pieces;

public class Move {
    int startRow;
    int startCol;
    int endRow;
    int endCol;

    String typeOfMove;
    PieceType pieceCaptured;
    PieceType pieceMoved;

    public Move(int startRow, int startCol, int endRow, int endCol, String typeOfMove, PieceType pieceCaptured, PieceType pieceMoved) {
        this.startRow = startRow;
        this.startCol = startCol;
        this.endRow = endRow;
        this.endCol = endCol;
        this.pieceCaptured = pieceCaptured;
        this.pieceMoved = pieceMoved;
        this.typeOfMove = typeOfMove;
    }

    public String toString() {
        return "Move from (" + startRow + "," + startCol + ") to (" + endRow + "," + endCol + ")" + pieceCaptured + pieceMoved;
    }

    public int getEndRow() {
        return endRow;
    }

    public int getEndCol() {
        return endCol;
    }

    public int getStartRow() {
        return startRow;
    }

}
