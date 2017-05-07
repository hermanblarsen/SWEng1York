package com.i2lp.edi.client.utilities;

/**
 * Created by habl on 24/02/2017.
 */
public class Theme {
    private String backgroundColour;
//    private Font font;
    private String font;
    private int fontSize;
    private String fontColour;
    private String graphicsColour;

    private boolean isBorder;
    private int borderThickness;
    private String borderColour;


    public Theme () {
        //set standard theme/ behaviour
        this.backgroundColour = "#FFFFFFFF";
        this.font = "\"Times New Roman\", Times, serif";
        this.fontSize = 12;
        this.fontColour = "#000000FF";
        this.graphicsColour = "000000FF";
        this.isBorder = true;
        this.borderThickness = 1;
        this.borderColour = "000000FF";
    }

    public String getBackgroundColour() {
        return backgroundColour;
    }

    public void setBackgroundColour(String backgroundColour) {
        this.backgroundColour = backgroundColour;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public String getGraphicsColour() {
        return graphicsColour;
    }

    public void setGraphicsColour(String graphicsColour) {
        this.graphicsColour = graphicsColour;
    }

    public void setFont(String font) {
        this.font = font;
    }

    public int getBorderThickness() {
        return borderThickness;
    }

    public void setBorderThickness(int borderThickness) {
        this.borderThickness = borderThickness;
    }

    public boolean isBorder() {
        return isBorder;
    }

    public void setBorder(boolean border) {
        isBorder = border;
    }

    public String getBorderColour() {
        return borderColour;
    }

    public void setBorderColour(String borderColour) {
        this.borderColour = borderColour;
    }

    public String getFont() {
        return font;
    }

    public String getFontColour() {
        return fontColour;
    }

    public void setFontColour(String fontColour) {
        this.fontColour = fontColour;
    }
}
