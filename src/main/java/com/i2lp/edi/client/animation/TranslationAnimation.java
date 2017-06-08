package com.i2lp.edi.client.animation;

import javafx.animation.*;
import javafx.util.Duration;

/**
 * Created by Zain on 18/04/2017.
 * Defines a controller for a simple point to point translation animation. </br>
 * The transition is linear from one defined value to another.  The user should ensure that the final and initial values specified
 * match the final/initial value which has been assigned to the element.  This prevents sudden changes and undesired behaviour when a redraw is called.
 */
public class TranslationAnimation extends Animation{
    private double startX;
    private double startY;
    private double endX;
    private double endY;
    private double width;
    private double height;
    private boolean scaleSet = false;

    /**
     * All coordinates are normalised to the canvas width/height.
     * @param startX Start x position
     * @param startY Start y position
     * @param endX End x position
     * @param endY End y position
     * @param durationMillis duration of transition in milliseconds
     */
    public TranslationAnimation(double startX, double startY, double endX, double endY, double durationMillis){
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.duration = Duration.millis(durationMillis);
    }

    public TranslationAnimation() {
    }

    /**
     * Scale factor for denormalisation of path coordinates. Should be set before calling play if the user intends to denormalise it.
     * @param width Horizontal scaling factor (typically slide width)
     * @param height Vertical scaling factor (typically slide height)
     */
    public void setScaleFactor(double width, double height){
        this.width = width;
        this.height = height;
        scaleSet = true;
    }

    /**
     * Begins the animation. </br>
     * Application of the scaling factor happens here.
     */
    @Override
    public void play() {
        if(!scaleSet){
            logger.error("Animation played before scale was set before calling play");
        }
        animation = new TranslateTransition(duration, getCoreNodeToAnimate());
        ((TranslateTransition)animation).setFromX(startX*width);
        ((TranslateTransition)animation).setFromY(startY*height);
        ((TranslateTransition)animation).setToX(endX*width);
        ((TranslateTransition)animation).setToY(endY*height);

        animation.setCycleCount(1);
        animation.play();
    }

    public void setStartX(double startX) {
        this.startX = startX;
    }

    public void setStartY(double startY) {
        this.startY = startY;
    }

    public void setEndX(double endX) {
        this.endX = endX;
    }

    public void setEndY(double endY) {
        this.endY = endY;
    }

    public double getStartX() {
        return startX;
    }

    public double getStartY() {
        return startY;
    }

    public double getEndX() {
        return endX;
    }

    public double getEndY() {
        return endY;
    }
}
