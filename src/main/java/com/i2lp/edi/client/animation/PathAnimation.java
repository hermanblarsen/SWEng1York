package com.i2lp.edi.client.animation;

import javafx.animation.Interpolator;
import javafx.animation.PathTransition;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.transform.Scale;
import javafx.util.Duration;


/**
 * Created by Zain on 18/04/2017.
 *
 * Defines an animation along a generic path.  The path taken is specified using the SVG path standard found at https://www.w3.org/TR/SVG11/paths.html#PathElement
 */
public class PathAnimation extends Animation{
    private SVGPath path;
    private double width = 1;
    private double height = 1;
    boolean scaleSet = false;

    public PathAnimation(String pathString, double durationMillis){
        duration = Duration.millis(durationMillis);
        setPath(pathString);
    }

    public PathAnimation(){

    }

    public void setScaleFactor(double width, double height){
        this.width = width;
        this.height = height;
        scaleSet = true;
    }

    public void play(){
        if(!scaleSet){
            logger.error("Animation played before scale was set before calling play");
        }
        Scale denormalisation= new Scale(width, height, 0,0);
        path.getTransforms().add(denormalisation);
        PathTransition transition = new PathTransition(duration, path, getCoreNodeToAnimate());
        transition.setCycleCount(1);
        transition.setInterpolator(Interpolator.EASE_BOTH);
        transition.play();
    }

    public void setPath(String pathString) {
        this.path = new SVGPath();
        this.path.setContent(pathString);
        this.path.setStroke(Color.RED);
        this.path.setStrokeWidth(5);
    }

    public SVGPath getPathUnscaled() {
        return path;
    }
}
