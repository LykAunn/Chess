package main;

import Pieces.PieceType;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Map;

public class PawnPromotionMenu {
    private GamePanel gp;
    private ArrayList<ButtonUI> promotionButtons;
    private boolean isVisible = false;
    private int menuX, menuY;
    private int menuWidth, menuHeight;
    private int promotingColor;
    private int promotingRow, promotingCol;
    private Rectangle menuBackground;

    private PromotionCallback callback;

    public interface PromotionCallback {
        void onPieceSelected(PieceType selectedPiece);
    }

    public PawnPromotionMenu(GamePanel gp) {
        this.gp = gp;
        this.promotionButtons = new ArrayList<>();
        this.menuWidth = gp.tileSize;
        this.menuHeight = gp.tileSize * 4;
    }

    public void showPromotionMenu(int row, int col, int color, PromotionCallback callback) {
        this.promotingRow = row;
        this.promotingCol = col;
        this.promotingColor = color;
        this.callback = callback;
        this.isVisible = true;

        this.menuX = gp.xShift + (col * gp.tileSize);
        this.menuY = row == 0 ? gp.yShift : gp.yShift + (gp.tileSize * 4);

        this.menuBackground = new Rectangle(menuX, menuY, menuWidth, menuHeight);

        createPromotionButtons();
    }

    private void createPromotionButtons() {
        promotionButtons.clear();

        String colorPrefix = (promotingColor == 0) ? "white" : "black";
        int buttonSize = gp.tileSize;
        int buttonSpacing = 0;
        int startY = menuY;
        int buttonX = menuX;

        Map<String, BufferedImage> pieceImages = gp.pieceManager.getPromotionImages();

        PieceType[] promotionPieces = {PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT};

        for (int i = 0; i < promotionPieces.length; i++) {
            PieceType pieceType = promotionPieces[i];
            String imageKey = colorPrefix + "_" + pieceType.name().toLowerCase();
            BufferedImage pieceImage = pieceImages.get(imageKey);

            int buttonY = startY + (i * (buttonSize + buttonSpacing));

            ButtonUI button = new ButtonUI(pieceImage, buttonX, buttonY, buttonSize, buttonSize, () -> {
                selectPiece(pieceType);
            });

            promotionButtons.add(button);
        }
    }

    private void selectPiece(PieceType selectedPiece) {
        if (callback != null) {
            callback.onPieceSelected(selectedPiece);
        }
        hideMenu();
    }

    public void hideMenu() {
        this.isVisible = false;
        this.promotionButtons.clear();
        this.callback = null;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void draw(Graphics2D g2d) {
        if (!isVisible) return;

        //Semi Transparent overlay over board
        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.fillRect(0, 0, gp.totalBoardWidth, gp.totalBoardHeight);

        //Menu background
        g2d.setColor(Color.WHITE);
        g2d.fillRoundRect(menuBackground.x, menuBackground.y, menuBackground.width, menuBackground.height, 10, 10);

        // Buttons
        for (ButtonUI button : promotionButtons) {
            button.draw(g2d);
        }
    }

    public boolean handleClick(int x, int y) {
        if (!isVisible) return false;

        // Check if clicked outside the menu (close menu)
        if (!menuBackground.contains(x, y)) {
            hideMenu();
            return true;
        }

        for (ButtonUI button : promotionButtons) {
            if (button.isClicked(x, y)) {
                button.click();
                return true;
            }
        }

        return true;

    }

    public void handleMouseMoved(int x, int y) {
        if (!isVisible) return;

        // Update button hover states
        for (ButtonUI button : promotionButtons) {
            boolean wasHovered = button.isHovered();
            boolean nowHovered = button.isClicked(x, y);

            if (nowHovered != wasHovered) {
                button.setHovered(nowHovered);
            }
        }
    }

    public void handleMousePressed(int x, int y) {
        if (!isVisible) return;

        for (ButtonUI button : promotionButtons) {
            if (button.isClicked(x, y)) {
                button.setPressed(true);
            }
        }
    }

    public void handleMouseReleased(int x, int y) {
        if (!isVisible) return;

        for (ButtonUI button : promotionButtons) {
            if (button.isPressed() && button.isClicked(x, y)) {
                button.click();
            }
            button.setPressed(false);
        }
    }
}