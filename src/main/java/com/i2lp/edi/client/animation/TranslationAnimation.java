package com.i2lp.edi.client.animation;

import javafx.animation.*;
import javafx.util.Duration;

/**
 * Created by Zain on 18/04/2017.
 */
public class TranslationAnimation extends Animation{
    private double startX;
    private double startY;
    private double endX;
    private double endY;
    private double width;
    private double height;
    private boolean scaleSet = false;

    public TranslationAnimation(double startX, double startY, double endX, double endY, double durationMillis){
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.duration = Duration.millis(durationMillis);
    }

    public TranslationAnimation(){

    }

    public void setScaleFactor(double width, double height){
        this.width = width;
        this.height = height;
        scaleSet = true;
    }

    @Override
    public void play() {
        if(!scaleSet){
            logger.error("Animation played before scale was set before calling play");
        }
        TranslateTransition transition = new TranslateTransition(duration, getCoreNodeToAnimate());
        transition.setFromX(startX*width);
        transition.setFromY(startY*height);
        transition.setToX(endX*width);
        transition.setToY(endY*height);

        transition.setCycleCount(1);
        transition.play();
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
