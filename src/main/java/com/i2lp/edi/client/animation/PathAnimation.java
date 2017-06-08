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
 * Defines an animation along a generic path.
 * The path taken is specified using the SVG path standard found at https://www.w3.org/TR/SVG11/paths.html#PathElement
 * The user should ensure that the final and initial values specified
 * match the final/initial value which has been assigned to the element.
 * This prevents sudden changes and undesired behaviour when a redraw is called.
 *
 */
public class PathAnimation extends Animation{
    private SVGPath path;
    private double width = 1;
    private double height = 1;
    boolean scaleSet = false;

    /**
     * Contstructs a path animation.
     * @param pathString Defined by W3C's SVG Path specification
     * @param durationMillis Time transition takes to complete (from first point to end point) in milliseconds
     */
    public PathAnimation(String pathString, double durationMillis){
        duration = Duration.millis(durationMillis);
        setPath(pathString);
    }

    public PathAnimation() {
    }

    /**
     * Scale factor for denormalisation of path coordinates. Should be set before calling play if the user intends to denormalise it.
     * @param width Horizontal scaling factor (typically slide width)
     * @param height Vertical scaling factor (typically slide height)
     */
    public void setScaleFactor(double width, double height){
        this.width = width;
        this.height = height;
        scaleSet = true;
    }

    /**
     * Begins the animation. </br>
     * Application of the scaling factor happens here.
     */
    public void play(){
        if(!scaleSet){
            logger.error("Animation played before scale was set before calling play");
        }
        Scale denormalisation= new Scale(width, height, 0,0);
        if(path.getTransforms().size() == 0) {
        	path.getTransforms().add(denormalisation);
        } else{
            path.getTransforms().set(0, denormalisation);
        }
        animation = new PathTransition(duration, path, getCoreNodeToAnimate());
        animation.setCycleCount(1);
        animation.setInterpolator(Interpolator.EASE_BOTH);
        animation.play();
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
