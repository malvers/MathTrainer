package mathtrainer;

import java.awt.*;
import java.awt.image.BufferedImage;
import org.scilab.forge.jlatexmath.*;

public class Latexer {

    /**
     * Renders a LaTeX formula to a BufferedImage with a transparent background.
     * @param latex The LaTeX string to render
     * @param fontSizeIn The base font size for rendering
     * @return A BufferedImage containing the rendered formula, or null if rendering failed.
     */
    public static BufferedImage renderLatexToImage(String latex, float fontSizeIn, float maxWidth, Color textColor) {

        float fontSize = fontSizeIn;
        try {

            TeXFormula formula = new TeXFormula(latex);
            TeXIcon icon = formula.createTeXIcon(TeXConstants.STYLE_DISPLAY, fontSize);
            BufferedImage image = new BufferedImage(
                    icon.getIconWidth(),
                    icon.getIconHeight(),
                    BufferedImage.TYPE_INT_ARGB
            );
            icon.setForeground(textColor);
            Graphics2D g2d = image.createGraphics();
            g2d = image.createGraphics();

            // Enable anti-aliasing for quality
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Set the background to transparent
            g2d.setComposite(AlphaComposite.Clear);
            g2d.fillRect(0, 0, icon.getIconWidth(), icon.getIconHeight());

            // Reset composite and paint the icon (it will use the color we set above)
            g2d.setComposite(AlphaComposite.SrcOver);
            icon.paintIcon(null, g2d, 0, 0);

            ///  TODO: why not always orange ???
            while(icon.getIconWidth() > maxWidth) {

                fontSize -= 10;

                // Parse the formula
                formula = new TeXFormula(latex);

                // Create an Icon from the LaTeX
                icon = formula.createTeXIcon(TeXConstants.STYLE_DISPLAY, fontSize);

                icon.setForeground(textColor);

                // Create a transparent image to render into
                image = new BufferedImage(
                        icon.getIconWidth(),
                        icon.getIconHeight(),
                        BufferedImage.TYPE_INT_ARGB
                );

                g2d = image.createGraphics();

                // Enable anti-aliasing for quality
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                // Set the background to transparent
                g2d.setComposite(AlphaComposite.Clear);
                g2d.fillRect(0, 0, icon.getIconWidth(), icon.getIconHeight());

                // Reset composite and paint the icon (it will use the color we set above)
                g2d.setComposite(AlphaComposite.SrcOver);
                icon.paintIcon(null, g2d, 0, 0);
            }

            g2d.dispose();
            return image;

        } catch (Exception e) {
            System.err.println("Error rendering LaTeX: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}