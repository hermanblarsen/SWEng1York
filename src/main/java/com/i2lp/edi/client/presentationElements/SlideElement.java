package com.i2lp.edi.client.presentationElements;

import com.i2lp.edi.client.animation.Animation;
import com.i2lp.edi.client.managers.PresentationController;
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

    protected float duration=-1;
    protected int slideID; //Needed for CSS generation, CSS filename needs this to identify what to apply

    protected String presentationID; //Needed for CSS generation
    protected int elementID;
    protected int layer=1;
    protected boolean visibility = true;
    protected int startSequence  = 0 ;
    protected int endSequence = -1;
    protected String onClickAction;
    protected String onClickInfo;
    protected Pane slideCanvas;
    Animation startAnimation, endAnimation;
    boolean onCanvas = false;
    protected double slideWidth;
    protected double slideHeight;
    protected boolean teacher;

    protected PresentationController presentationController;

    public abstract void doClassSpecificRender();

    public void removeElement(){
        destroyElement();
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
            logger.info("Bounds: " + getCoreNode().localToScene(getCoreNode().getBoundsInLocal()));
        }

        if (!(this instanceof AudioElement)) {
            //TODO: Trigger shared refresh
            switch (animationType) {
                case Animation.NO_ANIMATION: //No animation (click)
                    logger.info("No animation");
                    break;
                case Animation.ENTRY_ANIMATION: //Entry animation (playback)
                    if (startAnimation != null) {//animation Exists as StartSequence Present
                        startAnimation.play();
                        logger.info("Entry animation playing");
                    } else {
                        // If there's no animation to show the element then just make it visible
                        getCoreNode().setVisible(isVisibility());
                    }
                    break;
                case Animation.EXIT_ANIMATION: //Exit animation (playback)
                    if (endAnimation != null) {//animation Exists as EndSequence Present
                        endAnimation.play();
                        logger.info("Exit animation playing");
                    } else {
                        getCoreNode().setVisible(false);
                    }
                    break;
            }
        }
        if(animationType == Animation.EXIT_ANIMATION) {
            // Audio Element needs to be included in this
            destroyElement();
        }
    }

    public abstract Node getCoreNode();

    public abstract void setupElement();

    /**
     * Must be called whenever an element is no longer on screen / active.
     * This method should be used as an opportunity to halt, cleanup the element, and to return it to a state where no
     * elements of it have any effect.
     * If you disagree with this description then let me know -Zain
     */
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

    public boolean isTeacher() {
        return teacher;
    }

    public void setTeacher(boolean teacher) {
        this.teacher = teacher;
    }

    public double getSlideWidth() {
        return slideWidth;
    }

    public void setSlideWidth(double slideWidth) {
        this.slideWidth = slideWidth;
    }

    public double getSlideHeight() {
        return slideHeight;
    }

    public void setSlideHeight(double slideHeight) {
        this.slideHeight = slideHeight;
    }

    public void setPresentationController(PresentationController presentationController) {
        this.presentationController = presentationController;
    }

    protected void scaleDimensions(float xPosition, float yPosition){
        //Convert position percentages to multipliers against canvas size and update location
        getCoreNode().setTranslateX(xPosition * slideWidth);
        getCoreNode().setTranslateY(yPosition * slideHeight);
    }

    protected void performOnClickAction(){
        logger.info("Performing onClick action: " + onClickAction + " with onClick info: " + onClickInfo);
        switch(onClickAction){
            case "openwebsite":
                //onclickinfo=”URL”
                break;
            case "gotoslide":
                presentationController.goToSlide(Integer.parseInt(onClickInfo));
                break;

            case "dynamicmediatoggle":
                //onclickinfo = elementid
                break;

            case "none":
                logger.info("No OnClickAction for ElementID: " + getElementID());
                break;
        }
    }
}
