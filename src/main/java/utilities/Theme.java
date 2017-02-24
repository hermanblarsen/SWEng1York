package utilities;

import javafx.scene.text.Font;

/**
 * Created by habl on 24/02/2017.
 */
public class Theme {
    private String backgroundColour;
    private Font font;
    private int fontSize;
    private String graphicsColour;

    public Theme () {
        //set standard theme/ behaviour
        this.backgroundColour = "000000FF";
        this.font = Font.getDefault();
        this.fontSize = 12;
        this.graphicsColour = "FFFFFFFF";;
    }
}
