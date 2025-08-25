package main;

import Pieces.PieceManager;
import Pieces.PieceType;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class SideMenu {
    private GamePanel gp;
    private Graphics2D g2;
    private Font titleFont, textFont, smallFont;
    private final PieceManager pieceManager;

    // Captured Pieces
    private final ArrayList<PieceType> whiteCaptured = new ArrayList<>();
    private final ArrayList<PieceType> blackCaptured = new ArrayList<>();

    // Pawn promotion dialog
    private boolean showPromotionDialog = false;
    private int promotionRow, promotionCol;
    private PieceType selectedPiece = PieceType.QUEEN;

    public SideMenu(GamePanel gp) {
        this.gp = gp;
        pieceManager = new PieceManager(gp);
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
        int y = 20;

        // Menu Background
        g2.setColor(Color.GRAY);
        g2.fillRect(gp.menuStartX, 0, gp.menuWidth, gp.screenHeight);

        // Captured Pieces
        drawCapturedPieces(g2, y);

    }

    public void drawCapturedPieces(Graphics2D g2, int startY) {

        g2.setFont(textFont);
        g2.setColor(Color.WHITE);
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 20F));

        int x = gp.xShift;
        int y = startY;
        String side = gp.getwhiteBottom() ? "Black" : "White";
        String otherSide = gp.getwhiteBottom() ? "White" : "Black";

        //Title
        g2.drawString(side, x, y);
        y = gp.yShift / 2;

        Integer[] arrayCaptured = calculateNoOfCapturedPieces(whiteCaptured,blackCaptured);
        // White captured pieces (by black)
        pieceManager.renderCapturedPieces(g2, arrayCaptured, gp.getwhiteBottom(), y);

    }

    public void addWhiteCapturedPiece(PieceType piece) {
        whiteCaptured.add(piece);
    }

    public void addBlackCapturedPiece(PieceType piece) {
        blackCaptured.add(piece);
    }

    public Integer[] calculateNoOfCapturedPieces(ArrayList<PieceType> whiteCapturedPieces, ArrayList<PieceType> blackCapturedPieces) {
        int blackPawns = 0;
        int blackBishop = 0;
        int blackKnight = 0;
        int blackRook = 0;
        int blackQueen = 0;

        int whitePawns = 0;
        int whiteBishop = 0;
        int whiteKnight = 0;
        int whiteRook = 0;
        int whiteQueen = 0;

        for (PieceType piece : whiteCapturedPieces) {
            switch (piece) {
                case QUEEN:
                    whiteQueen++;
                    break;
                case BISHOP:
                    whiteBishop++;
                    break;
                case KNIGHT:
                    whiteKnight++;
                    break;
                case ROOK:
                    whiteRook++;
                    break;
                case PAWN:
                    whitePawns++;
            }
        }

        for (PieceType piece : blackCapturedPieces) {
            switch (piece) {
                case QUEEN:
                    blackQueen++;
                    break;
                case BISHOP:
                    blackBishop++;
                    break;
                case KNIGHT:
                    blackKnight++;
                    break;
                case ROOK:
                    blackRook++;
                    break;
                case PAWN:
                    blackPawns++;
            }
        }

        return new Integer[]{blackPawns, blackBishop, blackKnight, blackRook, blackQueen, whitePawns, whiteBishop, whiteKnight, whiteRook, whiteQueen};
    }
}
