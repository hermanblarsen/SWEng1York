package utilities;

/**
 * Created by Luke on 26/02/2017.
 */
public class Polygon {
    protected float[] xPositions;
    protected float[] yPositions;
    protected boolean isClosed;

    public Polygon(){

    }

    public Polygon(float[] xPositions, float[] yPositions, boolean isClosed){
        this.xPositions = xPositions;
        this.yPositions = yPositions;
        this.isClosed = isClosed;
    }

    public float[] getxPositions() {
        return xPositions;
    }

    public void setxPositions(float[] xPositions) {
        this.xPositions = xPositions;
    }

    public float[] getyPositions() {
        return yPositions;
    }

    public void setyPositions(float[] yPositions) {
        this.yPositions = yPositions;
    }

    public boolean isClosed() {
        return isClosed;
    }

    public void setClosed(boolean closed) {
        isClosed = closed;
    }
}
