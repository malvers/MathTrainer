package mathtrainer;

import javax.swing.*;
import java.awt.*;

public class TranslucentLabel extends JLabel {

    // Optional: Constructor to set initial text and opacity
    public TranslucentLabel(String text) {
        super(text);
        setOpaque(false); // This is CRUCIAL! Tells Swing we draw our own background.
    }

    @Override
    protected void paintComponent(Graphics g) {
        // 1. Create a translucent background
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f)); // 70% opaque
        g2d.setColor(getBackground());
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.dispose();

        // 2. Let the parent class (JLabel) draw the text, icon, borders, etc.
        // This will respect your alignment (CENTER, LEFT, etc.)
        super.paintComponent(g);
    }
}