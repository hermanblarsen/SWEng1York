package utilities;

/**
 * Created by Luke on 26/02/2017.
 */
public class Oval{
    protected float xPosition;
    protected float yPosition;
    protected float rVertical;
    protected float rHorizontal;
    protected float rotation;

    public Oval(){

    }

    /*public void setOvalFromParent (GraphicElement graphicElement) {
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

    public Oval(float xPosition, float yPosistion, float rVertical, float rHorizontal, float rotation){
        this.xPosition = xPosition;
        this.yPosition = yPosistion;
        this.rVertical = rVertical;
        this.rHorizontal = rHorizontal;
        this.rotation = rotation;
    }

    public float getxPosition() {
        return xPosition;
    }

    public void setxPosition(float xPosition) {
        this.xPosition = xPosition;
    }

    public float getyPosition() {
        return yPosition;
    }

    public void setyPosition(float yPosition) {
        this.yPosition = yPosition;
    }

    public float getrVertical() {
        return rVertical;
    }

    public void setrVertical(float rVertical) {
        this.rVertical = rVertical;
    }

    public float getrHorizontal() {
        return rHorizontal;
    }

    public void setrHorizontal(float rHorizontal) {
        this.rHorizontal = rHorizontal;
    }

    public float getRotation() {
        return rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }
}
