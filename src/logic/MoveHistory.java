package logic;

import Pieces.Move;
import Pieces.Piece;
import Pieces.PieceType;
import main.SideMenu;

import java.util.ArrayList;
import java.util.Objects;

public class MoveHistory {
    public ArrayList<Move> history;
    final boolean whiteOnBottom;
    public ArrayList<String> historyStrings;

    public MoveHistory(boolean whiteOnBottom) {
        this.whiteOnBottom = whiteOnBottom;
        history = new ArrayList<>();
        historyStrings = new ArrayList<>();
    }

    public void addMove(Move move) {
        history.add(move);
        String moveString = toAlgebraicString(move);
        historyStrings.add(moveString);
        SideMenu.addMoveToHistory(moveString);
    }

    public char getPieceSymbol(PieceType piece) {
        switch (piece) {
            case PAWN:
                return '\0';
            case KNIGHT:
                return 'N';
            case BISHOP:
                return 'B';
            case ROOK:
                return 'R';
            case QUEEN:
                return 'Q';
            case KING:
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
            rank = row + 1;
        }
        return String.valueOf(getFile(col)) + rank;
    }

    public char promotionSymbol(String typeOfMove) {
        if (typeOfMove == null) return '?';
        return switch (typeOfMove) {
            case "PROMOTEQUEEN" -> 'Q';
            case "PROMOTEROOK" -> 'R';
            case "PROMOTEKNIGHT" -> 'N';
            case "PROMOTEBISHOP" -> 'B';
            default -> '?';
        };
    }

    public String toAlgebraicString(Move move) {
        char captured = '\0';

        if (move.getPieceCaptured() != null) {
            captured = 'x';
        }

        // Castling Checks
        if (move.getPieceMoved() == PieceType.KING) {
            if (Objects.equals(move.getTypeOfMove(), "CASTLEKINGSIDE")) {
                return "o-o";
            } else if (Objects.equals(move.getTypeOfMove(), "CASTLEQUEENSIDE")) {
                return "o-o-o";
            }
        }

        // Pawn promotion check
        if ((move.getEndRow() == 7 && move.getStartRow() == 6) || (move.getEndRow() == 0 && move.getStartRow() == 1)) {
            return String.valueOf(getFile(move.getStartCol()))
                    + captured + squareName(move.getEndCol(), move.getEndRow()) + "=" + promotionSymbol(move.getTypeOfMove());
        }

        // Normal piece
        if (move.getPieceMoved() == PieceType.PAWN) {
            if (move.getPieceCaptured() != null) {
                return String.valueOf(getFile(move.getStartCol())) + captured + squareName(move.getEndCol(), move.getEndRow());
            } else {
                return squareName(move.getEndCol(), move.getEndRow());
            }
        } else {
            return String.valueOf(getPieceSymbol(move.getPieceMoved())) + captured + squareName(move.getEndCol(), move.getEndRow());
        }
    }
}
