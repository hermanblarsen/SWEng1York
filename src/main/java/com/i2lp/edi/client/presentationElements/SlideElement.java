package com.i2lp.edi.client.presentationElements;

import com.i2lp.edi.client.Animation.Animation;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by habl on 23/02/2017.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class SlideElement {
    Logger logger = LoggerFactory.getLogger(getClass());

    protected float duration;
    protected int slideID; //Needed for CSS generation, CSS filename needs this to identify what to apply

    protected String presentationID; //Needed for CSS generation
    protected int elementID;
    protected int layer;
    protected boolean visibility;
    protected int startSequence;
    protected int endSequence;
    protected String onClickAction;
    protected String onClickInfo;
    protected Pane slideCanvas;
    Animation startAnimation, endAnimation;
    boolean onCanvas = false;

    public abstract void doClassSpecificRender();

    public void removeElement(){
        if(onCanvas){
            slideCanvas.getChildren().remove(getCoreNode());
            onCanvas = false;
        }
    }

    //Empty interface for tagging our actual slide elements
    public void renderElement(int animationType) {
        //Added to the canvas at render time, as otherwise negates use of VisibleSet
        //If we bind to canvas, the element is always visible. Ignoring the sequencing and anims.
        //Add CoreNode to the Pane
        if (getCoreNode() == null) {
            logger.error("Tried to set slide internalCanvas before Element constructor was called!");
        } else {
            //Ensure we only add an element to the Canvas once.
            if(!onCanvas) {
                onCanvas = true;
                slideCanvas.getChildren().add(getCoreNode());
            }
            doClassSpecificRender();
        }

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
                    } else {
                        // If there's no animation to show the element then just make it visible
                        getCoreNode().setVisible(true);
                    }
                    break;
                case Animation.EXIT_ANIMATION: //Exit Animation (playback)
                    if (endAnimation != null) {//Animation Exists as EndSequence Present
                        endAnimation.play();
                        logger.info("Exit animation playing");
                    } else {
                        getCoreNode().setVisible(false);
                    }
                    destroyElement();
                    break;
            }
        }
    }

    public abstract Node getCoreNode();

    public abstract void setupElement();

    public abstract void destroyElement();

    public void setSlideCanvas(Pane slideCanvas) {
        this.slideCanvas = slideCanvas;
        setupElement();
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

    public void setSlideID(int slideID) {
        this.slideID = slideID;
    }

    public void setPresentationID(String presentationID) {
        this.presentationID = presentationID;
    }
}
