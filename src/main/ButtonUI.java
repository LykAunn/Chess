package main;

import java.awt.*;

public class ButtonUI {
    private String text;
    private Rectangle bounds;
    private Runnable action; // Action that happens when button is clicked
    private Font font;

    public ButtonUI(String text, int x, int y, Font font, Graphics2D g2, Runnable action) {
        this.text = text;
        this.font = font;
        g2.setFont(font);
        int width = g2.getFontMetrics().stringWidth(text);
        int height = g2.getFontMetrics().getHeight();
        this.bounds = new Rectangle(x, y - height, width, height);
        this.action = action;
    }

    public void draw(Graphics2D g2) {
        g2.setFont(font);
        g2.setColor(Color.blue);

        g2.fillRect(bounds.x - 10, bounds.y + 5, bounds.width + 20, bounds.height + 8);

        g2.setColor(Color.white);
        g2.drawString(text, bounds.x, bounds.y + bounds.height);
    }

    public boolean isClicked(int x, int y) {
        return bounds.contains(x, y);
    }

    public void click() {
        if (action != null) action.run();
    }
}
