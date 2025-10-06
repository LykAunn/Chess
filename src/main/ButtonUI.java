package main;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ButtonUI {
    private String text;
    private Rectangle bounds;
    private Runnable action; // Action that happens when button is clicked
    private Font font;
    private BufferedImage buttonImage, hoverImage, pressedImage;
    private boolean isHovered = false;
    private boolean isPressed = false;
    private ButtonType buttonType;

    public void setHovered(boolean nowHovered) {
        isHovered = !nowHovered;
    }

    public enum ButtonType {
        TEXT_ONLY,
        IMAGE_ONLY,
        IMAGE_WITH_TEXT;
    }

    // Constructor for text-only buttons
    public ButtonUI(String text, int x, int y, Font font, Graphics2D g2, Runnable action) {
        this.text = text;
        this.font = font;
        this.buttonType = ButtonType.TEXT_ONLY;
        g2.setFont(font);
        int width = g2.getFontMetrics().stringWidth(text);
        int height = g2.getFontMetrics().getHeight();
        this.bounds = new Rectangle(x, y - height, width, height);
        this.action = action;
    }

    // Constructor for image-only buttons
    public ButtonUI(BufferedImage image, int x, int y, Runnable action, int tileSize) {
        this.buttonType = ButtonType.IMAGE_ONLY;
        this.buttonImage = image;
        this.bounds = new Rectangle(x, y, buttonImage.getWidth(), buttonImage.getHeight());
        this.action = action;
    }

    // Constructor for image-only buttons with hover/pressed states
    public ButtonUI(BufferedImage buttonImage, BufferedImage hoverImage, BufferedImage pressedImage,
                    int x, int y, Runnable action) {
        this.buttonImage = buttonImage;
        this.hoverImage = hoverImage;
        this.pressedImage = pressedImage;
        this.buttonType = ButtonType.IMAGE_ONLY;
        this.bounds = new Rectangle(x, y, buttonImage.getWidth(), buttonImage.getHeight());
        this.action = action;
    }

    // Constructor for image with text overlay
    public ButtonUI(BufferedImage buttonImage, String text, Font font, int x, int y, Runnable action) {
        this.buttonImage = buttonImage;
        this.text = text;
        this.font = font;
        this.buttonType = ButtonType.IMAGE_WITH_TEXT;
        this.bounds = new Rectangle(x, y, buttonImage.getWidth(), buttonImage.getHeight());
        this.action = action;
    }

    // Constructor for scaled image buttons
    public ButtonUI(BufferedImage buttonImage, int x, int y, int width, int height, Runnable action) {
        this.buttonImage = buttonImage;
        this.buttonType = ButtonType.IMAGE_ONLY;
        this.bounds = new Rectangle(x, y, width, height);
        this.action = action;
    }

    public void draw(Graphics2D g2) {
        switch (buttonType) {
            case TEXT_ONLY:
                drawTextButton(g2);
                break;
            case IMAGE_ONLY:
                drawImageButton(g2);
                break;
            case IMAGE_WITH_TEXT:
                drawImageWithTextButton(g2);
                break;

        }
    }

    public void drawTextButton(Graphics2D g2) {
        g2.setFont(font);

        // Shadow
        g2.setColor(Color.black);
        g2.fillRect(bounds.x - 5, bounds.y + 10, bounds.width + 20, bounds.height + 8);

        // Background
        g2.setColor(Color.white);
        g2.fillRect(bounds.x - 10, bounds.y + 5, bounds.width + 20, bounds.height + 8);

        // Border
        g2.setColor(isPressed ? Color.DARK_GRAY : Color.BLACK);
        g2.drawRect(bounds.x - 10, bounds.y + 5, bounds.width + 20, bounds.height + 8);

        // Text
        g2.setColor(Color.black);
        g2.drawString(text, bounds.x, bounds.y + bounds.height);
    }

    public void drawImageButton(Graphics2D g2) {
        BufferedImage imageToDraw = buttonImage;

        if (isPressed && pressedImage != null) {
            imageToDraw = pressedImage;
        } else if (isHovered && hoverImage != null) {
            imageToDraw = hoverImage;
        }

        g2.drawImage(imageToDraw, bounds.x, bounds.y, bounds.width, bounds.height, null);

        // Visual feedback for hover/pressed states if no special images
        if (hoverImage == null || pressedImage == null) {
            if (isPressed) {
                // Add a dark overlay when pressed
                g2.setColor(new Color(0, 0, 0, 50));
                g2.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            } else if (isHovered) {
                // Add a light overlay when hovered
                g2.setColor(new Color(255, 255, 255, 30));
                g2.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            }
        }

        // Border
        if (isHovered || isPressed) {
            g2.setColor(isPressed ? Color.DARK_GRAY : Color.LIGHT_GRAY);
            g2.setStroke(new BasicStroke(2));
            g2.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
            g2.setStroke(new BasicStroke(1)); // Reset stroke
        }
    }

    private void drawImageWithTextButton(Graphics2D g2) {
        g2.drawImage(buttonImage, bounds.x, bounds.y, bounds.width, bounds.height, null);

        if (text != null && font != null) {
            g2.setFont(font);
            FontMetrics fm = g2.getFontMetrics();

            int textX = bounds.x + (bounds.width - fm.stringWidth(text)) / 2;
            int textY = bounds.y + (bounds.height + fm.getHeight()) / 2;

            // Text shadow
            g2.setColor(Color.black);
            g2.drawString(text, textX + 1, textY + 1);

            // Visual feedback
            if (isPressed) {
                g2.setColor(new Color(0, 0, 0, 80));
                g2.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            } else if (isHovered) {
                g2.setColor(new Color(255, 255, 255, 40));
                g2.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            }
        }
    }

    // Mouse interaction
    public boolean isClicked(int x, int y) {
        return bounds.contains(x, y);
    }

    public void click() {
        if (action != null) action.run();
    }

    public void setPressed(boolean pressed) {
        this.isPressed = pressed;
    }

    public boolean isHovered() {
        return isHovered;
    }

    public boolean isPressed() {
        return isPressed;
    }

    // Utility methods
    public Rectangle getBounds() {
        return bounds;
    }

    public void setBounds(int x, int y, int width, int height) {
        this.bounds = new Rectangle(x, y, width, height);
    }

    public void setPosition(int x, int y) {
        this.bounds.x = x;
        this.bounds.y = y;
    }
}