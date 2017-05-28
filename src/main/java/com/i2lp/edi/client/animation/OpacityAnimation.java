package com.i2lp.edi.client.animation;

import javafx.animation.FadeTransition;
import javafx.util.Duration;

/**
 * Created by Zain on 18/04/2017.
 */
public class OpacityAnimation extends Animation{
    private double startOpacity;
    private double endOpacity;

    public OpacityAnimation(double startOpacity, double endOpacity, double durationMillis){
        this.startOpacity = startOpacity;
        this.endOpacity = endOpacity;
        this.duration = Duration.millis(durationMillis);
    }

    public OpacityAnimation(){

    }

    @Override
    public void play() {
        FadeTransition transition = new FadeTransition(duration, getCoreNodeToAnimate());
        transition.setFromValue(startOpacity);
        transition.setToValue(endOpacity);

        transition.setCycleCount(1);
        transition.play();
    }

    public void setStartOpacity(double startOpacity) {
        this.startOpacity = startOpacity;
    }

    public void setEndOpacity(double endOpacity) {
        this.endOpacity = endOpacity;
    }

    public double getStartOpacity() {
        return startOpacity;
    }

    public double getEndOpacity() {
        return endOpacity;
    }
}
