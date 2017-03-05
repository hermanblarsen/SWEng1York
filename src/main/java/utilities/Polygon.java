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

    /*public void setPolygonFromParent (GraphicElement graphicElement) {
        //TODO is there a better way of doing this?? -Herman
        this.elementID = graphicElement.getElementID();
        this.layer = graphicElement.getLayer();
        this.visibility = graphicElement.isVisibility();
        this.startSequence = graphicElement.getStartSequence();
        this.endSequence = graphicElement.getEndSequence();
        this.duration = graphicElement.getDuration();
        this.onClickAction = graphicElement.getOnClickAction();
        this.onClickInfo = graphicElement.getOnClickInfo();
        this.aspectRatioLock = graphicElement.isAspectRatioLock();
        this.elementAspectRatio = graphicElement.getElementAspectRatio();
        this.lineColour = graphicElement.getLineColour();
        this.fillColour = graphicElement.getFillColour();
    }*/


    //TODO don't think this one is needed
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
