package com.i2lp.edi.client.animation;

import javafx.animation.FadeTransition;
import javafx.util.Duration;

/**
 * Created by Zain on 18/04/2017.
 *
 * OpacityAnimation defined a simple type of animation creating a transition of an element's opacity from one value to another.
 * The transition is linear from one defined value to another.  The user should ensure that the final and initial values specified
 * match the final/initial value which has been assigned to the element.  This prevents sudden changes and undesired behaviour when a redraw is called.
 */
public class OpacityAnimation extends Animation{
    private double startOpacity;
    private double endOpacity;

    /**
     * Create a new Fade/Opacity Animation
     * @param startOpacity Opacity level at the sart of the transition. (0 - 1 inc.)
     * @param endOpacity Opacity level at the end of the transition (0 - 1 inc. )
     * @param durationMillis Duration the transition takes to complete. (Milliseconds)
     */
    public OpacityAnimation(double startOpacity, double endOpacity, double durationMillis){
        this.startOpacity = startOpacity;
        this.endOpacity = endOpacity;
        this.duration = Duration.millis(durationMillis);
    }

    public OpacityAnimation() {
    }

    /**
     * Plays the animation once.  setOnFinish() can be used to add a listener which will fire at the end of the animation. </br>
     * Initialisation of the transition controllers happens here.
     */
    @Override
    public void play() {
        animation = new FadeTransition(duration, getCoreNodeToAnimate());
        ((FadeTransition)animation).setFromValue(startOpacity);
        ((FadeTransition)animation).setToValue(endOpacity);

        animation.setCycleCount(1);
        animation.play();
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
