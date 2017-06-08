package com.i2lp.edi.client.animation;

import javafx.animation.ScaleTransition;
import javafx.util.Duration;

/**
 * Created by Zain on 18/04/2017.
 * Defines a simeple two-point scale animation. </br>
 * The transition is linear from one defined value to another.  The user should ensure that the final and initial values specified
 * match the final/initial value which has been assigned to the element.  This prevents sudden changes and undesired behaviour when a redraw is called.
 */
public class ScaleAnimation extends Animation{
    private double startScale;
    private double endScale;

    /**
     * Constructs a scale animation.
     * @param startScale Start value (0 - 1 inc.)
     * @param endScale End value (0 - 1 inc.)
     * @param durationMillis Time transition takes (Milliseconds)
     */
    public ScaleAnimation(double startScale, double endScale, double durationMillis){
        this.startScale = startScale;
        this.endScale = endScale;
        this.duration = Duration.millis(durationMillis);
    }

    public ScaleAnimation() {
    }

    /**
     * Starts the animation.
     */
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
