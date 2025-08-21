package main;

import logic.GameState;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import static logic.GameState.TITLE;

public class UI {
    static GamePanel gp;
    Graphics2D g2;
    Font maruMonica, purisaB;
    ArrayList<ButtonUI> buttons = new ArrayList<>();
    public int titleScreenState = 0; // 0: First Menu, 1: 1/2 Player selection, 2: Play which side

    public UI(GamePanel gp) {
        this.gp = gp;

        try {
            InputStream is = getClass().getResourceAsStream("/font/x12y16pxMaruMonica.ttf");
            maruMonica = Font.createFont(Font.TRUETYPE_FONT, is);

            InputStream is2 = getClass().getResourceAsStream("/font/Purisa Bold.ttf");
            purisaB = Font.createFont(Font.TRUETYPE_FONT, is2);

        } catch (FontFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void handleClick(int x, int y) {
        if (gp.getGameState() == TITLE) {
            ButtonUI clickedButton = null;
            for (ButtonUI button : buttons) {
                if (button.isClicked(x, y)) {
                    clickedButton = button;
                    break; // stop after first hit
                }
            }
            if (clickedButton != null) {
                clickedButton.click();
            }
        }
    }

    public void draw(Graphics2D g2) {
        this.g2 = g2;
        g2.setFont(maruMonica);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);

        // TITLE STATE
        if (gp.getGameState() == TITLE) {
            drawTitleScreen();
        }
    }

    public void drawTitleScreen() {

        if(titleScreenState == 0) {
            g2.setColor(new Color(70, 120, 80));
            g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

            // TITLE NAME
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 200F));
            String text = "Chess";
            int x = getXforCenteredText(text);
            int y = gp.tileSize * 2;

            // SHADOW
            g2.setColor(Color.black);
            g2.drawString(text, x + 5, y + 5);

            // MAIN TEXT
            g2.setColor(Color.WHITE);
            g2.drawString(text, x, y);

            // PIECE IMAGES
            x = gp.screenWidth / 2 - (gp.tileSize / 2);
            y += gp.tileSize;

            g2.drawImage(gp.pieceManager.piece1, x - (gp.tileSize * 2), y, gp.tileSize, gp.tileSize, null);
            g2.drawImage(gp.pieceManager.piece2, x, y, gp.tileSize, gp.tileSize, null);
            g2.drawImage(gp.pieceManager.piece3, x + (gp.tileSize * 2), y, gp.tileSize, gp.tileSize, null);

            // MENU BUTTONS

            text = "NEW GAME";
            g2.setFont(g2.getFont().deriveFont(50F));
            x = getXforCenteredText(text);
            y += (int) (gp.tileSize * 2.5);
            if (buttons.isEmpty()) {
                buttons.add(new ButtonUI(text, x, y, g2.getFont().deriveFont(50F), g2, () -> {titleScreenState = 1; clearButton();}));

                text = "LOAD GAME";
                x = getXforCenteredText(text);
                y += (int) (gp.tileSize * 0.75);
                buttons.add(new ButtonUI(text, x, y, g2.getFont().deriveFont(50F), g2, () -> gp.setGameState(GameState.PLAYING)));

                text = "QUIT";
                x = getXforCenteredText(text);
                y += (int) (gp.tileSize * 0.75);
                buttons.add(new ButtonUI(text, x, y, g2.getFont().deriveFont(50F), g2, () -> System.exit(0)));
            }

        } else if (titleScreenState == 1) {
            g2.setColor(new Color(70, 120, 80));
            g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

            String text = "1 PLAYER";
            int x = gp.screenWidth / 2 - (gp.screenWidth / 4) - (g2.getFontMetrics().stringWidth(text) / 2);
            int y = gp.screenHeight / 2;
            buttons.add(new ButtonUI(text, x, y, g2.getFont().deriveFont(50F), g2, () -> System.exit(0)));

            text = "2 PLAYER";
            x = gp.screenWidth / 2 + (gp.screenWidth / 4) ;
            y = gp.screenHeight / 2;
            buttons.add(new ButtonUI(text, x, y, g2.getFont().deriveFont(50F), g2, () -> {titleScreenState = 2; clearButton();}));
        } else if (titleScreenState == 2) {

            g2.setColor(new Color(70, 120, 80));
            g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

            String text = "WHITE TOP";
            int x = gp.screenWidth / 2 - (gp.screenWidth / 4) - (g2.getFontMetrics().stringWidth(text) / 2);
            int y = gp.screenHeight / 2;
            buttons.add(new ButtonUI(text, x, y, g2.getFont().deriveFont(50F), g2, () -> {gp.setPlayerColor(false); gp.setGameState(GameState.PLAYING);}));

            text = "BLACK TOP";
            x = gp.screenWidth / 2 + (gp.screenWidth / 4) ;
            y = gp.screenHeight / 2;
            buttons.add(new ButtonUI(text, x, y, g2.getFont().deriveFont(50F), g2, () -> {gp.setPlayerColor(true); gp.setGameState(GameState.PLAYING);}));
        }


        for (ButtonUI button : buttons) {
            button.draw(g2);
        }

    }

    public int getXforCenteredText(String text) {
        int length = g2.getFontMetrics().stringWidth(text);
        int x = (gp.screenWidth / 2) - (length / 2);
        return x;
    }

    public void clearButton() {
        buttons.clear();
    }

}