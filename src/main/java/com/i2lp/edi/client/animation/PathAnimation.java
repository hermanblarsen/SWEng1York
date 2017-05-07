package com.i2lp.edi.client.animation;

import javafx.animation.Interpolator;
import javafx.animation.PathTransition;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;


/**
 * Created by Zain on 18/04/2017.
 *
 * Defines an animation along a generic path.  The path taken is specified using the SVG path standard found at https://www.w3.org/TR/SVG11/paths.html#PathElement
 */
public class PathAnimation extends Animation{
    private SVGPath path;

    public PathAnimation(String pathString, double durationMillis){
        duration = Duration.millis(durationMillis);
        this.path = new SVGPath();
        this.path.setContent(pathString);
        this.path.setStroke(Color.RED);
        this.path.setStrokeWidth(5);
    }

    public void play(){
        PathTransition transition = new PathTransition(duration, path, getCoreNodeToAnimate());
        transition.setCycleCount(1);
        transition.setInterpolator(Interpolator.EASE_BOTH);
        transition.play();
    }
}
