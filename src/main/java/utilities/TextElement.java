package utilities;

import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    protected String borderColour;

    protected float xPosition;
    protected float yPosition;
    protected float xSize;
    protected float ySize;

    protected boolean aspectRatioLock;
    protected float elementAspectRatio;

    protected WebView browser;
    protected WebEngine webEngine;

    public TextElement() {

    }

    @Override
    void setupElement() {
        //We need to just do this once, had to move this out of the TextElement constructor because of
        //Hermans JUnit XML Test, cant instantiate Nodes without a JavaFX Scene being present.

        //I moved this to a separate method that can be called whenever it is instantiated/Updated.
        //  Maybe surround this with try-catch statements as well as it has potential to go bad if we are not careful. - Herman
        //TODO @amrik find a suitable place for this method, and/or split it up and put it where it belongs. No setters can have methods referring to the rendered element
        browser = new WebView();
        webEngine = browser.getEngine();
        webEngine.documentProperty().addListener(new WebDocumentListener(webEngine));

        webEngine.loadContent(textContent);
        getCoreNode().setTranslateY(xPosition);
        getCoreNode().setTranslateX(xPosition);

        //TODO: Create Animations here, but move to setAnimation setter when XML implemented
        startAnimation = new Animation();
        startAnimation.setCoreNodeToAnimate(getCoreNode());
        startAnimation.setAnimationType(Animation.SIMPLE_APPEAR);
        endAnimation = new Animation();
        endAnimation.setCoreNodeToAnimate(getCoreNode());
        endAnimation.setAnimationType(Animation.SIMPLE_DISAPPEAR);
    }

    @Override
    void doClassSpecificRender() {
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


    @Override
    public Node getCoreNode() {
        return browser;
    }
}
