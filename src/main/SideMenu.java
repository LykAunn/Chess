package main;

import Pieces.PieceType;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class SideMenu {
    private GamePanel gp;
    private Graphics2D g2;
    private Font titleFont, textFont, smallFont;

    // Captured Pieces
    private ArrayList<PieceType> whiteCaptured = new ArrayList<>();
    private ArrayList<PieceType> blackCaptured = new ArrayList<>();

    // Pawn promotion dialog
    private boolean showPromotionDialog = false;
    private int promotionRow, promotionCol;
    private PieceType selectedPiece = PieceType.QUEEN;

    public SideMenu(GamePanel gp) {
        this.gp = gp;
        initializeFonts();
    }

    public void initializeFonts() {

        try {
            InputStream is = getClass().getResourceAsStream("/font/x12y16pxMaruMonica.ttf");
            titleFont = Font.createFont(Font.TRUETYPE_FONT, is);

            InputStream is2 = getClass().getResourceAsStream("/font/Purisa Bold.ttf");
            textFont = Font.createFont(Font.TRUETYPE_FONT, is2);

        } catch (FontFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        smallFont = new Font("Serif", Font.BOLD, 24);
    }

    public void draw(Graphics2D g2) {
        this.g2 = g2;
        int y = 10;

        // Menu Background
        g2.setColor(Color.GRAY);
        g2.fillRect(gp.menuStartX, 0, gp.menuWidth, gp.screenHeight);

        //

    }

    public void drawCapturedPieces(Graphics2D g2, int startY) {

        g2.setFont(titleFont);
        g2.setColor(Color.WHITE);

        int x = gp.menuStartX + 10;
        int y = startY;

        //Title
        g2.drawString("Captured", x, y);
        y += gp.tileSize * 2;

        // White captured pieces (by black)

    }

    public void addWhiteCapturedPiece(PieceType piece) {
        whiteCaptured.add(piece);
    }

    public void addBlackCapturedPiece(PieceType piece) {
        blackCaptured.add(piece);
    }
}
