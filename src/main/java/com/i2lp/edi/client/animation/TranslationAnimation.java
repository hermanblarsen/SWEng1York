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

    public TranslationAnimation(double startX, double startY, double endX, double endY, double durationMillis){
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.duration = Duration.millis(durationMillis);
    }

    public TranslationAnimation(){

    }



    @Override
    public void play() {
        TranslateTransition transition = new TranslateTransition(duration, getCoreNodeToAnimate());
        transition.setFromX(startX);
        transition.setFromY(startY);
        transition.setToX(endX);
        transition.setToY(endY);

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
