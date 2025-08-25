package Pieces;

import main.GamePanel;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import logic.Board;

public class PieceManager {

    GamePanel gp;
    private static final Map<String, BufferedImage> images = new HashMap<>();
    int pieceSize;
    int pieceShift;
    public BufferedImage piece1, piece2, piece3, spriteSheet;
    private Map<String, BufferedImage> spritesForCapturedPieces;
    int twoPieceXStep = 42;
    int pieceYStep = 36;
    int onePieceXStep = 26;
    private static final int[] PAWN_WIDTHS = {
            0,  // index 0 (unused)
            24, // 1 piece
            38, // 2 pieces
            53, // 3 pieces
            67, // 4 pieces
            80, // 5 pieces
            94, // 6 pieces
            108,// 7 pieces
            122 // 8 pieces
    };

    public PieceManager(GamePanel gp) {
        this.gp = gp;
        pieceSize = (int) (gp.tileSize * 0.9);
        pieceShift = (int) (gp.tileSize * 0.05);

        getImages();
        loadSpriteSheet();
        extractSpitePieces();

        debugCapturedPieces();
    }

    static {
        try {
            images.put("white_pawn", ImageIO.read(Objects.requireNonNull(PieceManager.class.getClassLoader().getResourceAsStream("pieces/WhitePawn.png"))));
            images.put("white_rook", ImageIO.read(Objects.requireNonNull(PieceManager.class.getClassLoader().getResourceAsStream("pieces/WhiteRook.png"))));
            images.put("white_knight", ImageIO.read(Objects.requireNonNull(PieceManager.class.getClassLoader().getResourceAsStream("pieces/WhiteKnight.png"))));
            images.put("white_queen", ImageIO.read(Objects.requireNonNull(PieceManager.class.getClassLoader().getResourceAsStream("pieces/WhiteQueen.png"))));
            images.put("white_king", ImageIO.read(Objects.requireNonNull(PieceManager.class.getClassLoader().getResourceAsStream("pieces/WhiteKing.png"))));
            images.put("white_bishop", ImageIO.read(Objects.requireNonNull(PieceManager.class.getClassLoader().getResourceAsStream("pieces/WhiteBishop.png"))));

            images.put("black_pawn", ImageIO.read(Objects.requireNonNull(PieceManager.class.getClassLoader().getResourceAsStream("pieces/BlackPawn.png"))));
            images.put("black_rook", ImageIO.read(Objects.requireNonNull(PieceManager.class.getClassLoader().getResourceAsStream("pieces/BlackRook.png"))));
            images.put("black_knight", ImageIO.read(Objects.requireNonNull(PieceManager.class.getClassLoader().getResourceAsStream("pieces/BlackKnight.png"))));
            images.put("black_queen", ImageIO.read(Objects.requireNonNull(PieceManager.class.getClassLoader().getResourceAsStream("pieces/BlackQueen.png"))));
            images.put("black_king", ImageIO.read(Objects.requireNonNull(PieceManager.class.getClassLoader().getResourceAsStream("pieces/BlackKing.png"))));
            images.put("black_bishop", ImageIO.read(Objects.requireNonNull(PieceManager.class.getClassLoader().getResourceAsStream("pieces/BlackBishop.png"))));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void debugCapturedPieces() {
        System.out.println("=== Captured Pieces Debug ===");
        System.out.println("SpriteSheet loaded: " + (spriteSheet != null));
        if (spriteSheet != null) {
            System.out.println("SpriteSheet dimensions: " + spriteSheet.getWidth() + "x" + spriteSheet.getHeight());
        }

        System.out.println("Total sprites extracted: " + (spritesForCapturedPieces != null ? spritesForCapturedPieces.size() : 0));

        if (spritesForCapturedPieces != null) {
            // Check specifically for black pawn sprites
            for (int i = 1; i <= 8; i++) {
                String key = i + "_black_pawn";
                BufferedImage sprite = spritesForCapturedPieces.get(key);
                System.out.println("Key: " + key + " -> " + (sprite != null ? "EXISTS" : "MISSING"));
            }
        }
        System.out.println("===============================");
    }

    public void getImages() {
        try  {
            piece1 = ImageIO.read(getClass().getClassLoader().getResourceAsStream("pieces/WhiteKing.png"));
            piece2 = ImageIO.read(getClass().getClassLoader().getResourceAsStream("pieces/BlackPawn.png"));
            piece3 = ImageIO.read(getClass().getClassLoader().getResourceAsStream("pieces/wHITEBishop.png"));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void renderPieces(Graphics2D g, Board board) {

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {

                Piece piece = board.getPiece(row, col);

                if (piece != null) {

                    if (gp.isDragSource(row,col)) {
                        continue; // Ignore render at that cell
                    }

                    String color = (piece.color == 1) ? "black" : "white";
                    String key = color + "_" + piece.getType().name().toLowerCase();
                    BufferedImage image = images.get(key);

                    if (image != null) {
                        int x = (col * gp.tileSize) + gp.xShift;
                        int y = (row * gp.tileSize) + gp.yShift;
                        g.drawImage(image, x + pieceShift, y + pieceShift, pieceSize , pieceSize, null);
                    }
                }
            }
        }
    }

    public void renderDraggedPiece(Graphics2D g, Piece piece, int mouseX, int mouseY) {
        if (piece == null) return;

        String color = (piece.color == 1) ? "black" : "white";
        String key = color + "_" + piece.getType().name().toLowerCase();
        BufferedImage image = images.get(key);

        if (image != null) {

            int dragSize = (int) (gp.tileSize * 1.05);
            int x = mouseX - dragSize / 2;
            int y = mouseY - dragSize / 2;

            g.drawImage(image, x, y, dragSize, dragSize, null);
        }
    }

    private int getPawnWidth(int pawnCount) {
        if (pawnCount >= 1 && pawnCount <= 8) {
            return PAWN_WIDTHS[pawnCount];
        }
        return PAWN_WIDTHS[1]; // fallback to 1 piece width
    }

    private int getOtherWidth(int count) {
        if (count == 1) {
            return onePieceXStep;
        } else if (count == 2) {
            return twoPieceXStep;
        } else {
            return 0;
        }
    }

    public void renderCapturedPieces(Graphics2D g, Integer[] noOfCapturedPieces , boolean whiteOnBottom, int startY) {
        int firstYToRender = startY;
        int secondYToRender = gp.yShift + gp.boardHeight + gp.yShift / 2;
        int useWhichYOnTop = whiteOnBottom ? firstYToRender : secondYToRender;
        int otherYToRender = whiteOnBottom ? secondYToRender : firstYToRender;
        int capturedPieceHeight = gp.yShift / 2;
        int currentX1 = gp.xShift;
        int currentX2 = gp.xShift;
        int spacing = gp.tileSize / 10;

        if (spritesForCapturedPieces == null || spritesForCapturedPieces.isEmpty()) {
            return;
        }

        // Extract counts from array
        int whitePawns = noOfCapturedPieces[0];
        int whiteBishop = noOfCapturedPieces[1];
        int whiteKnight = noOfCapturedPieces[2];
        int whiteRook = noOfCapturedPieces[3];
        int whiteQueen = noOfCapturedPieces[4];
        int blackPawns = noOfCapturedPieces[5];
        int blackBishop = noOfCapturedPieces[6];
        int blackKnight = noOfCapturedPieces[7];
        int blackRook = noOfCapturedPieces[8];
        int blackQueen = noOfCapturedPieces[9];

        if (whitePawns > 0) {
            BufferedImage image = spritesForCapturedPieces.get(whitePawns + "_white_pawn");
            if (image != null) {
                int dynamicWidth = getPawnWidth(whitePawns);

                // Scale the width proportionally
                int scaledWidth = (dynamicWidth * capturedPieceHeight) / image.getHeight();

                g.drawImage(image, currentX1, useWhichYOnTop, scaledWidth, capturedPieceHeight, null);
                currentX1 += scaledWidth + spacing; // Add some padding
            }
        }
        if (whiteBishop > 0) {
            BufferedImage image = spritesForCapturedPieces.get(whiteBishop + "_white_bishop");
            if (image != null) {
                int dynamicWidth = getPawnWidth(whiteBishop);

                // Scale the width proportionally
                int scaledWidth = (dynamicWidth * capturedPieceHeight) / image.getHeight();

                g.drawImage(image, currentX1, useWhichYOnTop, scaledWidth, capturedPieceHeight, null);
                currentX1 += scaledWidth + spacing; // Add some padding
            }
        }
        if (whiteKnight > 0) {
            BufferedImage image = spritesForCapturedPieces.get(whiteKnight + "_white_knight");
            if (image != null) {
                int dynamicWidth = getPawnWidth(whiteKnight);

                // Scale the width proportionally
                int scaledWidth = (dynamicWidth * capturedPieceHeight) / image.getHeight();

                g.drawImage(image, currentX1, useWhichYOnTop, scaledWidth, capturedPieceHeight, null);
                currentX1 += scaledWidth + spacing; // Add some padding
            }
        }
        if (whiteRook > 0) {
            BufferedImage image = spritesForCapturedPieces.get(whiteRook + "_white_rook");
            if (image != null) {
                int dynamicWidth = getPawnWidth(whiteRook);

                // Scale the width proportionally
                int scaledWidth = (dynamicWidth * capturedPieceHeight) / image.getHeight();

                g.drawImage(image, currentX1, useWhichYOnTop, scaledWidth, capturedPieceHeight, null);
                currentX1 += scaledWidth + spacing; // Add some padding
            }
        }
        if (whiteQueen > 0) {
            BufferedImage image = spritesForCapturedPieces.get(whiteQueen + "_white_queen");
            if (image != null) {
                int dynamicWidth = getPawnWidth(whiteQueen);

                // Scale the width proportionally
                int scaledWidth = (dynamicWidth * capturedPieceHeight) / image.getHeight();

                g.drawImage(image, currentX1, useWhichYOnTop, scaledWidth, capturedPieceHeight, null);
                currentX1 += scaledWidth + spacing; // Add some padding
            }
        }

        if (blackPawns > 0) {
            BufferedImage image = spritesForCapturedPieces.get(blackPawns + "_black_pawn");
            if (image != null) {
                int dynamicWidth = getPawnWidth(blackPawns);

                // Scale the width proportionally
                int scaledWidth = (dynamicWidth * capturedPieceHeight) / image.getHeight();

                g.drawImage(image, currentX2, otherYToRender, scaledWidth, capturedPieceHeight, null);
                currentX2 += scaledWidth + spacing; // Add some padding
            }
        }
        if (blackBishop > 0) {
            BufferedImage image = spritesForCapturedPieces.get(blackBishop + "_black_bishop");
            if (image != null) {
                int dynamicWidth = getPawnWidth(blackBishop);

                // Scale the width proportionally
                int scaledWidth = (dynamicWidth * capturedPieceHeight) / image.getHeight();

                g.drawImage(image, currentX2, otherYToRender, scaledWidth, capturedPieceHeight, null);
                currentX2 += scaledWidth + spacing; // Add some padding
            }
        }
        if (blackKnight > 0) {
            BufferedImage image = spritesForCapturedPieces.get(blackKnight + "_black_knight");
            if (image != null) {
                int dynamicWidth = getPawnWidth(blackKnight);

                // Scale the width proportionally
                int scaledWidth = (dynamicWidth * capturedPieceHeight) / image.getHeight();

                g.drawImage(image, currentX2, otherYToRender, scaledWidth, capturedPieceHeight, null);
                currentX2 += scaledWidth + spacing; // Add some padding
            }
        }
        if (blackRook > 0) {
            BufferedImage image = spritesForCapturedPieces.get(blackRook + "_black_rook");
            if (image != null) {
                int dynamicWidth = getPawnWidth(blackRook);

                // Scale the width proportionally
                int scaledWidth = (dynamicWidth * capturedPieceHeight) / image.getHeight();

                g.drawImage(image, currentX2, otherYToRender, scaledWidth, capturedPieceHeight, null);
                currentX2 += scaledWidth + spacing; // Add some padding
            }
        }
        if (blackQueen > 0) {
            BufferedImage image = spritesForCapturedPieces.get(blackQueen + "_black_queen");
            if (image != null) {
                int dynamicWidth = getPawnWidth(blackQueen);

                // Scale the width proportionally
                int scaledWidth = (dynamicWidth * capturedPieceHeight) / image.getHeight();

                g.drawImage(image, currentX2, otherYToRender, scaledWidth, capturedPieceHeight, null);
                currentX2 += scaledWidth + spacing; // Add some padding
            }
        }
    }

    public BufferedImage extractSprite(int startX, int startY, int endX, int endY) {
        return spriteSheet.getSubimage(startX, startY, (endX - startX), (endY - startY));
    }

    public void extractSpitePieces() {
        spritesForCapturedPieces = new HashMap<>();

        if (spriteSheet == null) return;

        int step = 50;

        spritesForCapturedPieces.put("8_black_pawn", extractSprite(1, 5, 123 , 35));
        spritesForCapturedPieces.put("7_black_pawn", extractSprite(1, 5 + step, 109 , 5 + step + 31));
        spritesForCapturedPieces.put("6_black_pawn", extractSprite(1, 5 + (step * 2), 95 , 5 + (step * 2) + 31));
        spritesForCapturedPieces.put("5_black_pawn", extractSprite(1, 5 + (step * 3), 81 , 5 + (step * 3) + 31));
        spritesForCapturedPieces.put("4_black_pawn", extractSprite(1, 5 + (step * 4), 67 , 5 + (step * 4) + 31));
        spritesForCapturedPieces.put("3_black_pawn", extractSprite(1, 5 + (step * 5), 53 , 5 + (step * 5) + 31));
        spritesForCapturedPieces.put("2_black_pawn", extractSprite(1, 5 + (step * 6), 38 , 5 + (step *6) + 31));
        spritesForCapturedPieces.put("1_black_pawn", extractSprite(1, 5 + (step * 7), 24 , 5 + (step * 7) + 31));
        spritesForCapturedPieces.put("2_black_bishop", extractSprite(135, 0, 135 + twoPieceXStep , pieceYStep));
        spritesForCapturedPieces.put("1_black_bishop", extractSprite(135,  step, 135 + onePieceXStep , step + pieceYStep));
        spritesForCapturedPieces.put("2_black_knight", extractSprite(190, 0, 190 + twoPieceXStep , pieceYStep));
        spritesForCapturedPieces.put("1_black_knight", extractSprite(190, step, 190 + onePieceXStep , step + pieceYStep));
        spritesForCapturedPieces.put("2_black_rook", extractSprite(241, 0, 241 + twoPieceXStep , pieceYStep));
        spritesForCapturedPieces.put("1_black_rook", extractSprite(241, step, 241 + onePieceXStep , step + pieceYStep));
        spritesForCapturedPieces.put("1_black_queen", extractSprite(290, 0, 290 + onePieceXStep , pieceYStep));

        spritesForCapturedPieces.put("8_white_pawn", extractSprite(360, 5,  360 + 123 , 35));
        spritesForCapturedPieces.put("7_white_pawn", extractSprite(360, 5 + step, 360 + 109 , 5 + step + 31));
        spritesForCapturedPieces.put("6_white_pawn", extractSprite(360, 5 + (step * 2), 360 + 95 , 5 + (step * 2) + 31));
        spritesForCapturedPieces.put("5_white_pawn", extractSprite(360, 5 + (step * 3), 360 + 81 , 5 + (step * 3) + 31));
        spritesForCapturedPieces.put("4_white_pawn", extractSprite(360, 5 + (step * 4), 360 + 67 , 5 + (step * 4) + 31));
        spritesForCapturedPieces.put("3_white_pawn", extractSprite(360, 5 + (step * 5), 360 + 53 , 5 + (step * 5) + 31));
        spritesForCapturedPieces.put("2_white_pawn", extractSprite(360, 5 + (step * 6), 360 + 38 , 5 + (step *6) + 31));
        spritesForCapturedPieces.put("1_white_pawn", extractSprite(360, 5 + (step * 7), 360 + 24 , 5 + (step * 7) + 31));
        spritesForCapturedPieces.put("2_white_bishop", extractSprite(136 + 360, 0, 136 + 360 + twoPieceXStep , pieceYStep));
        spritesForCapturedPieces.put("1_white_bishop", extractSprite(136 + 360,  step, 136 + 360 + onePieceXStep , step + pieceYStep));
        spritesForCapturedPieces.put("2_white_knight", extractSprite(190 + 360, 0, 190 + 360 + twoPieceXStep , pieceYStep));
        spritesForCapturedPieces.put("1_white_knight", extractSprite(190 + 360, step, 190 + 360 + onePieceXStep , step + pieceYStep));
        spritesForCapturedPieces.put("2_white_rook", extractSprite(241 + 360, 0, 241 + 360 + twoPieceXStep , pieceYStep));
        spritesForCapturedPieces.put("1_white_rook", extractSprite(241 + 360, step, 241 + 360 + onePieceXStep , step + pieceYStep));
        spritesForCapturedPieces.put("1_white_queen", extractSprite(290 + 360, 0, 290 + 360 + onePieceXStep , pieceYStep));

    }

    private void loadSpriteSheet() {
        try  {
            spriteSheet = ImageIO.read(Objects.requireNonNull(PieceManager.class.getClassLoader().getResourceAsStream("pieces/captured-pieces.png")));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}