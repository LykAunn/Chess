package Tile;

import logic.Board;
import main.GamePanel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.util.Objects;

public class TileManager {

    GamePanel panel;
    public Tile[] tile;
    int dotSize = 0;
    int dotShift = 0;

    public TileManager(GamePanel panel) {
        tile = new Tile[5];
        this.panel = panel;
        getTileImage();
        dotSize = (int) ((int) panel.tileSize * 0.4);
        dotShift = (int) ((int) panel.tileSize * 0.3);
    }

    public void getTileImage() {
        try {
            tile[0] = new Tile();
            tile[0].image = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("board/BlackTile.png")));
            if (tile[0].image == null) System.out.println("BlackTile.png not loaded");

            tile[1] = new Tile();
            tile[1].image = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("board/WhiteTile.png")));

            tile[2] = new Tile();
            tile[2].image = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("board/HighlightTile.png")));

            tile[3] = new Tile();
            tile[3].image = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("board/circle1.png")));

            tile[4] = new Tile();
            tile[4].image = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("board/circle2.png")));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void render(Graphics2D g2, Board board) {

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {

                int x = (col * panel.tileSize) + panel.xShift;
                int y = (row * panel.tileSize) + panel.yShift;
                int tileIndex = (row + col) % 2 == 0 ? 1 : 0;

                if (panel.isSelected(row, col)) {
                    g2.drawImage(tile[2].image, x, y, panel.tileSize, panel.tileSize, null);
                } else {
                    g2.drawImage(tile[tileIndex].image, x, y, panel.tileSize, panel.tileSize, null);
                }


            }
        }
    }

    public void renderDot(Graphics2D g2, Board board) {

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {

                int x = (col * panel.tileSize) + panel.xShift;
                int y = (row * panel.tileSize) + panel.yShift;

                if (panel.isPossibleMove(row, col)) {
                    if (Board.board[row][col] != null) {
                        g2.drawImage(tile[4].image, x, y, panel.tileSize, panel.tileSize, null);
                    } else {
                        g2.drawImage(tile[3].image, x + dotShift, y + dotShift, dotSize, dotSize, null);
                    }

                }
            }
        }


    }
}
