package com.i2lp.edi.client.Animation;

import javafx.animation.FadeTransition;
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

    @Override
    public void play() {
        ScaleTransition transition = new ScaleTransition(duration, getCoreNodeToAnimate());
        transition.setFromX(startScale);
        transition.setFromY(startScale);

        transition.setToX(endScale);
        transition.setToY(endScale);

        transition.setCycleCount(1);
        transition.play();
    }
}
