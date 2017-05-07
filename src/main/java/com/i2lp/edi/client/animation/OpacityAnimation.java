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

    @Override
    public void play() {
        FadeTransition transition = new FadeTransition(duration, getCoreNodeToAnimate());
        transition.setFromValue(startOpacity);
        transition.setToValue(endOpacity);

        transition.setCycleCount(1);
        transition.play();
    }
}
