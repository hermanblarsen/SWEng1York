package com.i2lp.edi.client.presentationElements;

import com.i2lp.edi.client.utilities.Utilities;
import com.sun.webkit.WebPage;
import javafx.concurrent.Worker;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Screen;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.i2lp.edi.client.Constants.TEXT_ELEMENT_ZOOM_FACTOR;

/**
 * Created by habl on 26/02/2017.
 */

public class TextElement extends SlideElement {
    protected String textContent = "Error: No Text Content Fund!";
    protected String textFilepath;
    protected String textContentReference;
    protected String font;
    protected int fontSize;
    protected String fontColour;
    protected String bgColour;

    private int borderSize = 1;
    protected String borderColour;


    protected boolean hasBorder;

    protected float xPosition = 0.25f;
    protected float yPosition = 0.25f;
    protected float xSize = 0.5f;
    protected float ySize = 0.5f;

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
        //Stage 1 Setup: Instantiate Core Node
        browser = new WebView();

        //Prevent browser from reacting to events as specified by default for all Webviews
        browser.addEventFilter(MouseEvent.ANY, event -> event.consume());

        webEngine = browser.getEngine();

        //Stage 2 Setup: Load Content into Core Node
        //Put HTML into WebView
        webEngine.loadContent(textContent);

        //Apply Dynamically created CSS to TextElement
        cssFilePath = Utilities.cssGen(presentationID, slideID, elementID, fontSize, font, fontColour, bgColour, borderColour, borderSize, hasBorder);
        webEngine.setUserStyleSheetLocation(cssFilePath);

        //Stage 3 Setup: DoClassSpecificRender for resizing, register State Handlers, register onClickAction method in superclass
        doClassSpecificRender();
        getCoreNode().addEventFilter(MouseEvent.MOUSE_CLICKED, event -> performOnClickAction());

        //If done rendering, adjust height to fit content, set isReady variable for thumbnail generation
        browser.getEngine().getLoadWorker().stateProperty().addListener((arg0, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                isReady = true;
            }
            //Set Background of WebView to transparent (using reflection) as CSS wont do the job on Node level
            try {
                Pattern c = Pattern.compile("rgba?\\((\\d{1,3})[,\\)](\\d{1,3})[,\\)](\\d{1,3})[,\\)](\\d+\\.\\d+)\\)?");
                if(bgColour == null) bgColour = "rgba(0,0,0,0.0)"; //If no background colour set, transparent background
                Matcher m = c.matcher (bgColour);

                if (m.matches())
                {
                    // Use reflection to retrieve the WebEngine's private 'page' field.
                    Field f = webEngine.getClass().getDeclaredField("page");
                    f.setAccessible(true);
                    WebPage page = (WebPage) f.get(webEngine);
                    Color backgroundColor = new java.awt.Color(Integer.valueOf(m.group(1)), Integer.valueOf(m.group(2)), Integer.valueOf(m.group(3)),  (int) (Float.valueOf(m.group(4))*255));
                    page.setBackgroundColor(backgroundColor.getRGB());
                }
            } catch (Exception e) {
                logger.error("Unable to set TextElement background colour to: " + bgColour , e);
            }
        });
        browser.setVisible(visibility);
    }

    @Override
    public void destroyElement() {

    }

    @Override
    public void doClassSpecificRender() {
        //Stage 1 DoClassSpecificRender: Resize width and height (Check AspectRatioLock), then X and Y positions based on canvas size.
        //Rescale X and Y sizes of text box
        if (slideCanvas.getScene() != null) {
            //If AspectRatio Locked for element, calculate Y size as product of X size with ElementAspectRatio
            if (isAspectRatioLock()) {
                browser.setPrefWidth(xSize * slideWidth);
                browser.setPrefHeight(browser.getPrefWidth() * elementAspectRatio);
            } else {
                browser.setPrefWidth(xSize * slideWidth);
                browser.setPrefHeight(ySize * slideHeight);
            }

            //Rescale positioning of elements
            scaleDimensions(xPosition, yPosition);
            //Alter Zoom of webview to maintain proportions
            browser.setZoom((slideWidth/ Screen.getPrimary().getVisualBounds().getWidth()) * TEXT_ELEMENT_ZOOM_FACTOR);
        }

        //Stage 2 DoClassSpecificRender: Refresh content inside Core Node
        //Refresh Browser
        browser.requestLayout();
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
        if (xPosition > 1) {
            logger.warn("Malformed XML. X Position for ElementID: " + elementID + " is larger than 100% of slide width. Defaulting to 50%.");
            this.xPosition = 0.5f;
        } else if (xPosition < 0) {
            logger.warn("Malformed XML. X Position for ElementID: " + elementID + " is smaller than 0% of slide width. Defaulting to 50%.");
            this.xPosition = 0f;
        } else {
            this.xPosition = xPosition;
        }
    }

    public float getyPosition() {
        return yPosition;
    }

    public void setyPosition(float yPosition) {
        if (yPosition > 1) {
            logger.warn("Malformed XML. Y Position for ElementID: " + elementID + " is larger than 100% of slide height. Defaulting to 50%.");
            this.yPosition = 0.5f;
        } else if (yPosition < 0) {
            logger.warn("Malformed XML. Y Position for ElementID: " + elementID + " is smaller than 0% of slide height. Defaulting to 50%.");
            this.yPosition = 0f;
        } else {
            this.yPosition = yPosition;
        }
    }

    public float getxSize() {
        return xSize;
    }

    public void setxSize(float xSize) {
        if (xSize > 1) {
            logger.warn("Malformed XML. X Size for ElementID: " + elementID + " is larger than 100% of slide width. Defaulting to 50%.");
            this.xSize = 1f;
        } else if (xSize < 0) {
            logger.warn("Malformed XML. X Size for ElementID: " + elementID + " is smaller than 0% of slide width. Defaulting to 50%.");
            this.xSize = 0.05f;
        } else {
            this.xSize = xSize;
        }
    }

    public float getySize() {
        return ySize;
    }

    public void setySize(float ySize) {
        if (ySize > 1) {
            logger.warn("Malformed XML. Y Size for ElementID: " + elementID + " is larger than 100% of slide height. Defaulting to 50%.");
            this.ySize = 1f;
        } else if (ySize < 0) {
            logger.warn("Malformed XML. Y Size for ElementID: " + elementID + " is smaller than 0% of slide height. Defaulting to 50%.");
            this.ySize = 0.05f;
        } else {
            this.ySize = ySize;
        }
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
        setHasBorder(true);
    }

    public boolean isRendered() {
        return isReady;
    }

    @Override
    public Node getCoreNode() {
        return browser;
    }

    public boolean isHasBorder() {
        return hasBorder;
    }

    public void setHasBorder(boolean hasBorder) {
        this.hasBorder = hasBorder;
    }
}
