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
    private int borderThickness;
    private boolean isBorder;
    private String borderColour;
    private float presentationAspectRatio;


    public Theme () {
        //set standard theme/ behaviour
        this.backgroundColour = "";
        this.font = "";
        this.fontSize = 0;
        this.fontColour = "";
        this.graphicsColour = "";
        this.borderThickness = 0;
        this.isBorder = true;
        this.borderColour = "";
        this.presentationAspectRatio = 1.33f;
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

    public float getPresentationAspectRatio() {
        return presentationAspectRatio;
    }

    public void setPresentationAspectRatio(float presentationAspectRatio) {
        this.presentationAspectRatio = presentationAspectRatio;
    }
}
