package utilities;

import javafx.scene.Node;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by habl on 23/02/2017.
 */
public abstract class SlideElement {
    Logger logger = LoggerFactory.getLogger(SlideElement.class);

    protected float duration;
    protected int elementID;
    protected int layer;
    protected boolean visibility;
    protected int startSequence;
    protected int endSequence;

    protected String onClickAction;
    protected String onClickInfo;
    protected Pane slideCanvas;
    Animation startAnimation, endAnimation;

    abstract void doClassSpecificRender();

    //Empty interface for tagging our actual slide elements
    void renderElement(int animationType) {
        doClassSpecificRender();

        if (!(this instanceof AudioElement)) {
            //TODO: Trigger shared refresh
            switch (animationType) {
                case Animation.NO_ANIMATION: //No animation (click)
                    logger.info("No animation");
                    break;
                case Animation.ENTRY_ANIMATION: //Entry Animation (playback)
                    if (startAnimation != null) {//Animation Exists as StartSequence Present
                        startAnimation.play();
                        logger.info("Entry animation playing");
                    }
                    break;
                case Animation.EXIT_ANIMATION: //Exit Animation (playback)
                    if (endAnimation != null) {//Animation Exists as EndSequence Present
                        endAnimation.play();
                        logger.info("Exit animation playing");
                    }
                    break;
            }
        }
    }

    abstract Node getCoreNode();

    abstract void setupElement();

    public void setSlideCanvas(Pane slideCanvas) {
        this.slideCanvas = slideCanvas;
        setupElement();

        //Add CoreNode to the Pane
        if (getCoreNode() == null) {
            logger.error("Tried to set slide internalCanvas before Element constructor was called!");
        } else {
            slideCanvas.getChildren().add(getCoreNode());
        }
    }

    public float getDuration() {
        return duration;
    }

    public void setDuration(float duration) {
        this.duration = duration;
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

    public boolean isVisibility() {
        return visibility;
    }

    public void setVisibility(boolean visibility) {
        this.visibility = visibility;
    }

    public int getLayer() {
        return layer;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }

    public int getElementID() {
        return elementID;
    }

    public void setElementID(int elementID) {
        this.elementID = elementID;
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
}
