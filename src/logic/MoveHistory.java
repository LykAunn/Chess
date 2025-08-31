package logic;

import Pieces.Move;
import Pieces.Piece;
import Pieces.PieceType;

import java.util.ArrayList;

public class MoveHistory {
    public ArrayList<Move> history;
    final boolean whiteOnBottom;
    public ArrayList<String> historyStrings;

    public MoveHistory(boolean whiteOnBottom) {
        this.whiteOnBottom = whiteOnBottom;
    }

    public void addMove(Move move) {
        history.add(move);
    }

    public char getPieceSymbol(PieceType piece) {
        switch (piece) {
            case PieceType.PAWN:
                return '\0';
            case PieceType.KNIGHT:
                return 'N';
            case PieceType.BISHOP:
                return 'B';
            case PieceType.ROOK:
                return 'R';
            case PieceType.QUEEN:
                return 'Q';
            case PieceType.KING:
                return 'K';
            default:
                return '?';
        }
    }

    public char getFile(int col) {
        return (char) ('a' + col);
    }

    public String squareName(int col, int row) {
        int rank;
        if (whiteOnBottom) {
            rank = 8 - row;
        } else {
            rank = row;
        }
        return getFile(col) + Integer.toString(rank);
    }

    public char promotionSymbol(String typeOfMove) {
        switch (typeOfMove) {
            case "PROMOTEQUEEN":
                return 'Q';
            case "PROMOTEROOK":
                return 'R';
            case "PROMOTEKNIGHT":
                return 'N';
            case "PROMOTEBISHOP":
                return 'B';
            default:
                return '?';
        }
    }

    public String toAlgebraicString(Move move) {
        char captured = '\0';

        if (move.getPieceCaptured() != null) {
            captured = 'x';
        }

        // Castling Checks
        if (whiteOnBottom) {
            if (move.getEndCol() - move.getStartCol() == 2) {
                return "o-o";
            } else if (move.getEndCol() - move.getStartCol() == -2) {
                return "o-o-o";
            }
        } else {
            if (move.getEndRow() - move.getStartRow() == 2) {
                return "o-o-o";
            } else if (move.getEndRow() - move.getStartRow() == -2) {
                return "o-o";
            }
        }

        // Pawn promotion check
        if ((move.getEndRow() == 8 && move.getStartRow() == 7) || (move.getEndRow() == 0 && move.getStartRow() == 1)) {
            return getFile(move.getStartCol()) + captured + squareName(move.getEndCol(), move.getEndRow()) + "=" + promotionSymbol(move.getTypeOfMove());
        }

        // Normal piece
        if (move.getPieceMoved() == PieceType.PAWN) {
            if (move.getPieceCaptured() != null) {
                return getFile(move.getStartCol()) + captured + squareName(move.getEndCol(), move.getEndRow());
            } else {
                return squareName(move.getEndCol(), move.getEndRow());
            }
        } else {
            return getPieceSymbol(move.getPieceMoved()) + captured + squareName(move.getEndCol(), move.getEndRow());
        }
    }
}
