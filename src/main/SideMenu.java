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
    private static String[] moveHistory;
    private static int moveCounter;
    private static int numberShift = 0;

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
        moveHistory = new String[16];


        moveCounter = 0;
        initializeFonts();
        pieceManager.loadSpriteSheet();
        pieceManager.extractSpitePieces();

        pieceManager.debugCapturedPieces();
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
        drawMoveHistory(g2, gp.menuStartX, y);


        // Captured Pieces
        drawCapturedPieces(g2, y);

    }

    public void drawMoveHistory(Graphics2D g2, int x, int y) {
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 30F));
        g2.setColor(Color.BLACK);
        FontMetrics fm = g2.getFontMetrics();

        int yToDraw = y;
        int lineHeight = fm.getHeight();

        // assume moveHistory is like: ["e4", "e5", "Nf3", "Nc6", ...]
        for (int i = 0; i < moveHistory.length; i += 2) {
            if (moveHistory[i] == null) return;
            int moveNumber = (i / 2) + 1 + numberShift;

            // Start X each line
            int xToDraw = x;

            // Draw move number
            String numString = moveNumber + ".";
            g2.drawString(numString, xToDraw, yToDraw);
            xToDraw += fm.stringWidth(numString + "  ");

            // Draw White’s move (always exists)
            g2.drawString(moveHistory[i], xToDraw, yToDraw);
            xToDraw += fm.stringWidth(moveHistory[i] + "  ");

            // Draw Black’s move (only if it exists)
            if (i + 1 < moveHistory.length && moveHistory[i + 1] != null) {
                g2.drawString(moveHistory[i + 1], xToDraw, yToDraw);
            }

            // Move to next line
            yToDraw += lineHeight;
        }
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
        y = gp.yShift + gp.boardHeight + startY;
        g2.drawString(otherSide, x, y);
        y = gp.yShift / 2;

        Integer[] arrayCaptured = calculateNoOfCapturedPieces(whiteCaptured,blackCaptured);
        // Captured Pieces
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

    public static void addMoveToHistory(String move) {
        if (moveCounter < moveHistory.length) {
            moveHistory[moveCounter] = move;
            moveCounter++;
        } else {
            shiftList();
            numberShift++;
            moveCounter = moveCounter -2;
            moveHistory[moveCounter] = move;

        }
    }

    public static void shiftList() {
        for (int i = 0; i < moveHistory.length - 2; i++) {
            moveHistory[i] = moveHistory[i + 2];
        }

        moveHistory[moveHistory.length - 2] = null;
        moveHistory[moveHistory.length - 1] = null;
    }
}