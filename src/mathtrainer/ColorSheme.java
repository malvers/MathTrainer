package mathtrainer;

import java.awt.*;

public class ColorSheme {

    public static Color darkBlue = new Color(0, 0, 40);
    public static Color orange = Color.ORANGE;
    public static Color niceGreen = new Color(113, 197, 37);
    public Color backGround = Color.WHITE;
    public Color fgLight = Color.DARK_GRAY;
    public Color fgDark = Color.DARK_GRAY;

    public ColorSheme(Color bg, Color fg, Color light) {

        backGround = bg;
        fgDark = fg;
        fgLight = light;
    }
}
