package com.i2lp.edi.client.animation;

import javafx.animation.ScaleTransition;
import javafx.util.Duration;

/**
 * Created by Zain on 18/04/2017.
 */
public class ScaleAnimation extends Animation{
    private double startScale;
    private double endScale;

    public ScaleAnimation(double startScale, double endScale, double durationMillis){
        this.startScale = startScale;
        this.endScale = endScale;
        this.duration = Duration.millis(durationMillis);
    }

    public ScaleAnimation(){

    }

    @Override
    public void play() {
        animation = new ScaleTransition(duration, getCoreNodeToAnimate());
        ((ScaleTransition)animation).setFromX(startScale);
        ((ScaleTransition)animation).setFromY(startScale);

        ((ScaleTransition)animation).setToX(endScale);
        ((ScaleTransition)animation).setToY(endScale);

        animation.setCycleCount(1);
        animation.play();
    }

    public void setStartScale(double startScale) {
        this.startScale = startScale;
    }

    public void setEndScale(double endScale) {
        this.endScale = endScale;
    }

    public double getStartScale() {
        return startScale;
    }

    public double getEndScale() {
        return endScale;
    }
}
