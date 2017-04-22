package com.i2lp.edi.client.presentationElements;

import com.i2lp.edi.client.utilities.Utils;
import javafx.concurrent.Worker;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

/**
 * Created by habl on 26/02/2017.
 */

public class TextElement extends SlideElement {
    protected String textContent;
    protected String textFilepath;
    protected String textContentReference;
    protected String font;
    protected int fontSize;
    protected String fontColour;
    protected String bgColour;

    private int borderSize;
    protected String borderColour;

    protected float xPosition;
    protected float yPosition;
    protected float xSize;
    protected float ySize;

    protected boolean aspectRatioLock;
    protected float elementAspectRatio;

    protected WebView browser;
    public WebEngine webEngine;
    String cssFilePath;
    private boolean isReady; //State for Webview

    public TextElement() {

    }

    @Override
    public void setupElement() {
        browser = new WebView();
        webEngine = browser.getEngine();

        //Put HTML into WebView
        webEngine.loadContent(textContent);

        //Apply Dynamically created CSS to TextElement
        //TODO: Ensure these match what we read in from XML (correct format). Add Getters and setters for Presentation GUID, SlideID then hook into this method call
        cssFilePath = Utils.cssGen(presentationID, slideID, elementID, fontSize, font, fontColour, bgColour, borderColour, borderSize);

        //If done rendering, adjust height to fit content, set isReady variable for thumbnail generation
        browser.getEngine().getLoadWorker().stateProperty().addListener((arg0, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                isReady = true;
            }
        });

        webEngine.setUserStyleSheetLocation(cssFilePath);

        getCoreNode().setTranslateY(xPosition);
        getCoreNode().setTranslateX(xPosition);

        getCoreNode().addEventHandler(MouseEvent.MOUSE_CLICKED, event -> logger.info("Clicked textElement!"));
        getCoreNode().setPickOnBounds(false);

    }

    @Override
    public void destroyElement() {
    }

    @Override
    public void doClassSpecificRender() {
        //Refresh Browser
        browser.requestLayout();




        //Update Location
        getCoreNode().setTranslateY(xPosition/100);
        getCoreNode().setTranslateX(xPosition);
    }

    public Pane getSlideCanvas() {
        return slideCanvas;
    }

    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    public String getTextFilepath() {
        return textFilepath;
    }

    public void setTextFilepath(String textFilepath) {
        this.textFilepath = textFilepath;
    }

    public String getTextContentReference() {
        return textContentReference;
    }

    public void setTextContentReference(String textContentReference) {
        this.textContentReference = textContentReference;
    }

    public float getxPosition() {
        return xPosition;
    }

    public void setxPosition(float xPosition) {
        this.xPosition = xPosition;
    }

    public float getyPosition() {
        return yPosition;
    }

    public void setyPosition(float yPosition) {
        this.yPosition = yPosition;
    }

    public float getxSize() {
        return xSize;
    }

    public void setxSize(float xSize) {
        this.xSize = xSize;
    }

    public float getySize() {
        return ySize;
    }

    public void setySize(float ySize) {
        this.ySize = ySize;
    }

    public String getFont() {
        return font;
    }

    public void setFont(String font) {
        this.font = font;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public String getFontColour() {
        return fontColour;
    }

    public void setFontColour(String fontColour) {
        this.fontColour = fontColour;
    }

    public String getBgColour() {
        return bgColour;
    }

    public void setBgColour(String bgColour) {
        this.bgColour = bgColour;
    }

    public String getBorderColour() {
        return borderColour;
    }

    public void setBorderColour(String borderColour) {
        this.borderColour = borderColour;
    }

    public boolean isAspectRatioLock() {
        return aspectRatioLock;
    }

    public void setAspectRatioLock(boolean aspectRatioLock) {
        this.aspectRatioLock = aspectRatioLock;
    }

    public float getElementAspectRatio() {
        return elementAspectRatio;
    }

    public void setElementAspectRatio(float elementAspectRatio) {
        this.elementAspectRatio = elementAspectRatio;
    }

    public int getBorderSize() {
        return borderSize;
    }

    public void setBorderSize(int borderSize) {
        this.borderSize = borderSize;
    }

    public boolean isRendered() {
        return isReady;
    }

    @Override
    public Node getCoreNode() {
        return browser;
    }
}
