package main;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Mouse extends MouseAdapter {

    private GamePanel gamePanel;
    public int x, y;
    public boolean pressed;

    // Drag states
    private boolean isDragging = false;
    private int dragStartX, dragStartY;
    private static final int DRAG_THRESHOLD = 5;

    public Mouse(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    public void mouseClicked(MouseEvent e) {
        x = e.getX();
        y = e.getY();
        gamePanel.handleClick(x, y);
        gamePanel.callUIToHandleClick(x, y);
    }

    public void mousePressed(MouseEvent e) {
        pressed = true;
        x = e.getX();
        y = e.getY();
        dragStartX = x;
        dragStartY = y;
        isDragging = false;

        gamePanel.handleMousePressed(x, y);
    }

    public void mouseReleased(MouseEvent e) {
        pressed = false;
        x = e.getX();
        y = e.getY();

        if (isDragging) {
            gamePanel.handleDragEnd(x, y);
            isDragging = false;
        }
    }

    public void mouseDragged(MouseEvent e) {
        x = e.getX();
        y = e.getY();

        if (!isDragging && pressed) {
            int deltaX = Math.abs(x - dragStartX);
            int deltaY = Math.abs(y - dragStartY);

            if (deltaX > DRAG_THRESHOLD || deltaY > DRAG_THRESHOLD) {
                isDragging = true;
                gamePanel.handleDragStart(dragStartX, dragStartY);
            }
        }

        // Update drag if dragging
        if (isDragging) {
            gamePanel.handleDragUpdate(x, y);
        }
    }

    public void mouseMoved(MouseEvent e) {
        x = e.getX();
        y = e.getY();
    }

    public boolean isDragging() {
        return isDragging;
    }

}
