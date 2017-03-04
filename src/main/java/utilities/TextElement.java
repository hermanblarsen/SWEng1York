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
public class TextElement implements SlideElement {
    protected int elementID;
    protected int layer;
    protected boolean visibility;
    protected int startSequence;
    protected int endSequence;
    protected float duration;
    protected String textContent;
    protected String textFilepath;
    protected String textContentReference;
    protected float xPosition;
    protected float yPosition;
    protected float xSize;
    protected float ySize;
    protected String font;
    protected int fontSize;
    protected String fontColour;
    protected String bgColour;
    protected String borderColour;
    protected String onClickAction;
    protected String onClickInfo;
    protected boolean aspectRatioLock;
    protected float elementAspectRatio;

    protected Animation startAnimation, endAnimation;


    Logger logger = LoggerFactory.getLogger(TextElement.class);
    protected Pane slideCanvas;
    protected WebView browser;
    protected WebEngine webEngine;

    public TextElement() {
        //We need to just do this once, had to move this out of the TextElement constructor because of
        //Hermans JUnit XML Test, cant instantiate Nodes without a JavaFX Scene being present.
        browser = new WebView();
        webEngine = browser.getEngine();
        webEngine.documentProperty().addListener(new WebDocumentListener(webEngine));
    }


    public Pane getSlideCanvas() {
        return slideCanvas;
    }

    public void setSlideCanvas(Pane slideCanvas) {
        this.slideCanvas = slideCanvas;
        //Add WebBrowser Element to the Pane
        if (browser == null) {
            logger.error("Tried to set slide internalCanvas before TextElement constructor was called!");
        } else {
            slideCanvas.getChildren().add(browser);
        }
    }

    public int getElementID() {
        return elementID;
    }

    public void setElementID(int elementID) {
        this.elementID = elementID;
    }

    public int getLayer() {
        return layer;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }

    public boolean isVisibility() {
        return visibility;
    }

    public void setVisibility(boolean visibility) {
        this.visibility = visibility;
    }

    public int getStartSequence() {
        return startSequence;
    }

    public void setStartSequence(int startSequence) {
        this.startSequence = startSequence;
    }

    public int getEndSequence() {
        return endSequence;
    }

    public void setEndSequence(int endSequence) {
        this.endSequence = endSequence;
    }

    public float getDuration() {
        return duration;
    }

    public void setDuration(float duration) {
        this.duration = duration;
    }

    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
        webEngine.loadContent(textContent);
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

    public float getyPosition() {
        return yPosition;
    }

    public void setyPosition(float yPosition) {
        this.yPosition = yPosition;
        getCoreNode().setTranslateY(xPosition);
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

    public String getOnClickAction() {
        return onClickAction;
    }

    public void setOnClickAction(String onClickAction) {
        this.onClickAction = onClickAction;
    }

    public String getOnClickInfo() {
        return onClickInfo;
    }

    public void setOnClickInfo(String onClickInfo) {
        this.onClickInfo = onClickInfo;
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

    public float getxPosition() {
        return xPosition;
    }

    public void setxPosition(float xPosition) {
        this.xPosition = xPosition;
        getCoreNode().setTranslateX(xPosition);
    }

    @Override
    public void renderElement(int animationType) {
        //Trigger some kind of redraw on browser
        browser.requestLayout();

        switch (animationType) {
            case Animation.NO_ANIMATION: //No animation (click)
                logger.info("No animation");
                break;
            case Animation.ENTRY_ANIMATION: //Entry Animation (playback)
                startAnimation.play();
                logger.info("Entry animation playing");
                break;
            case Animation.EXIT_ANIMATION: //Exit Animation (playback)
                endAnimation.play();
                logger.info("Exit animation playing");
                break;
        }
    }

    @Override
    public Node getCoreNode() {
        return browser;
    }
}
