package Pieces;

import main.GamePanel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import logic.Board;

public class PieceManager {

    GamePanel gp;
    private static final Map<String, BufferedImage> images = new HashMap<>();
    int pieceSize;
    int pieceShift;
    public BufferedImage piece1, piece2, piece3;

    public PieceManager(GamePanel gp) {
        this.gp = gp;
        pieceSize = (int) (gp.tileSize * 0.9);
        pieceShift = (int) (gp.tileSize * 0.05);

        getImages();

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
                        int x = col * gp.tileSize;
                        int y = row * gp.tileSize;
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

}