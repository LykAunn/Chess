package logic;

import Pieces.Move;

import java.util.ArrayList;

public interface HistoryObserver {
    void onMoveAdded(String move);
}
