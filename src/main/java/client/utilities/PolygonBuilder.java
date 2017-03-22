package client.utilities;


import javafx.scene.shape.Polyline;
import javafx.scene.shape.Shape;

/**
 * Created by Luke on 26/02/2017.
 * Modified by Zain 12/03/2017
 */
public class PolygonBuilder {
    protected float[] xPositions;
    protected float[] yPositions;
    protected boolean isClosed;

    private Shape shape;

    public PolygonBuilder(){

    }

    public Shape getShape()
    {
        if(shape==null){
            build();
        }
        return shape;
    }

    public PolygonBuilder(float[] xPositions, float[] yPositions, boolean isClosed){
        this.xPositions = xPositions;
        this.yPositions = yPositions;
        this.isClosed = isClosed;
    }

    public Shape build(){
        if(xPositions == null || yPositions == null ){
            throw new IllegalStateException("Position array not set.");
        }

        if (isClosed){
            shape = new javafx.scene.shape.Polygon(generateSinglePointsArray());
        } else {
            shape = new Polyline(generateSinglePointsArray());
        }
        return shape;
    }

    private double[] generateSinglePointsArray(){
        if(xPositions.length != yPositions.length){
            throw new IllegalStateException("Number of x positions does not match the number of y positions.");
        }
        double[] pointsArray = new double[xPositions.length + yPositions.length];
        for(int i=0; i< xPositions.length; i++){
            pointsArray[i*2]= xPositions[i];
            pointsArray[i*2+1] = yPositions[i];
        }
        return pointsArray;
    }

    public float[] getxPositions() {
        return xPositions;
    }

    public PolygonBuilder setxPositions(float[] xPositions) {
        this.xPositions = xPositions;
        return this;
    }

    public float[] getyPositions() {
        return yPositions;
    }

    public PolygonBuilder setyPositions(float[] yPositions) {
        this.yPositions = yPositions;
        return this;
    }

    public boolean isClosed() {
        return isClosed;
    }

    public PolygonBuilder setClosed(boolean closed) {
        isClosed = closed;
        return this;
    }
}
