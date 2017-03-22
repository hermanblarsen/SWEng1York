package client.utilities;

import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Shape;

/**
 * Created by Luke on 26/02/2017.
 * Modified by Zain 12/03/2017
 */
public class OvalBuilder {
    protected Float xPosition;
    protected Float yPosition;
    protected Float rVertical;
    protected Float rHorizontal;
    protected Float rotation;

    private Shape shape;

    public OvalBuilder(){

    }

    public Shape getShape(){
        if(shape==null){
            build();
        }
        return shape;
    }

    public Shape build(){
        if(xPosition == null || yPosition==null || rHorizontal==null || rVertical==null || rotation==null){
            throw new IllegalStateException("A property was not set.");
        }

        shape = new Ellipse(xPosition, yPosition, rVertical, rHorizontal);
        return shape;
    }

    public OvalBuilder(float xPosition, float yPosistion, float rVertical, float rHorizontal, float rotation){
        this.xPosition = xPosition;
        this.yPosition = yPosistion;
        this.rVertical = rVertical;
        this.rHorizontal = rHorizontal;
        this.rotation = rotation;
    }

    public float getxPosition() {
        return xPosition;
    }

    public OvalBuilder setxPosition(float xPosition) {
        this.xPosition = xPosition;
        return this;
    }

    public float getyPosition() {
        return yPosition;
    }

    public OvalBuilder setyPosition(float yPosition) {
        this.yPosition = yPosition;
        return this;
    }

    public float getrVertical() {
        return rVertical;
    }

    public OvalBuilder setrVertical(float rVertical) {
        this.rVertical = rVertical;
        return this;
    }

    public float getrHorizontal() {
        return rHorizontal;
    }

    public OvalBuilder setrHorizontal(float rHorizontal) {
        this.rHorizontal = rHorizontal;
        return this;
    }

    public float getRotation() {
        return rotation;
    }

    public OvalBuilder setRotation(float rotation) {
        this.rotation = rotation;
        return this;
    }
}
