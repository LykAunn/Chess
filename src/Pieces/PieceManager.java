package Pieces;

import main.GamePanel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PieceManager {

    static GamePanel gp;
    private static final Map<String, BufferedImage> images = new HashMap<>();
    static int pieceSize;
    static int pieceShift;

    public PieceManager(GamePanel gp) {
        this.gp = gp;
        pieceSize = (int) (gp.tileSize * 0.9);
        pieceShift = (int) (gp.tileSize * 0.05);

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

    public static void renderPieces(Graphics2D g) {

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = gp.board[row][col];
                if (piece != null) {
                    String color;
                    if (piece.color == 1) {
                        color = "white";
                    } else {
                        color = "black";
                    }

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

}
