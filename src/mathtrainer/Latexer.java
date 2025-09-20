package mathtrainer;

import java.awt.*;
import java.awt.image.BufferedImage;
import org.scilab.forge.jlatexmath.*;

public class Latexer {

    /**
     * Renders a LaTeX formula to a BufferedImage with a transparent background.
     * @param latex The LaTeX string to render
     * @param fontSize The base font size for rendering
     * @return A BufferedImage containing the rendered formula, or null if rendering failed.
     */
    public static BufferedImage renderLatexToImage(String latex, float fontSize) {
        Color textColor = Color.orange;
        try {
            // Parse the formula
            TeXFormula formula = new TeXFormula(latex);

            // Create an Icon from the LaTeX
            TeXIcon icon = formula.createTeXIcon(TeXConstants.STYLE_DISPLAY, fontSize);

            // ##### CRITICAL FIX: Set the color on the icon itself #####
            icon.setForeground(textColor);

            // Create a transparent image to render into
            BufferedImage image = new BufferedImage(
                    icon.getIconWidth(),
                    icon.getIconHeight(),
                    BufferedImage.TYPE_INT_ARGB // Crucial: ARGB for transparency
            );

            Graphics2D g2d = image.createGraphics();

            // Enable anti-aliasing for quality
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Set the background to transparent
            g2d.setComposite(AlphaComposite.Clear);
            g2d.fillRect(0, 0, icon.getIconWidth(), icon.getIconHeight());

            // Reset composite and paint the icon (it will use the color we set above)
            g2d.setComposite(AlphaComposite.SrcOver);
            icon.paintIcon(null, g2d, 0, 0);

            g2d.dispose();
            return image;

        } catch (Exception e) {
            System.err.println("Error rendering LaTeX: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}