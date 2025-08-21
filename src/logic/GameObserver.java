package logic;

import Pieces.Move;

import java.util.ArrayList;

public interface GameObserver {
    void onMoveExecuted(Move move);
    void onPieceSelected(int row, int col, ArrayList<Move> possibleMoves);
    void onSelectionCleared();
    void onGameStateChanged(GameState newState);
    void onTurnChanged(int currentPlayer);

}