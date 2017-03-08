package utilities;

import javafx.scene.Node;

/**
 * Created by habl on 25/02/2017.
 */
public class ImageElement extends SlideElement {

    //Dummy class for parsing
    protected float xPosition;
    protected float yPosition;
    protected float xSize;
    protected float ySize;
    protected String path;
    protected float opacity;
    protected boolean aspectRatioLock;
    protected float elementAspectRatio;
    private float durationSequence;

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

    public float getxSize() {
        return xSize;
    }

    public void setxSize(float xSize) {
        this.xSize = xSize;
    }

    public float getySize() {
        return ySize;
    }

    public void setySize(float ySize) {
        this.ySize = ySize;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public float getOpacity() {
        return opacity;
    }

    public void setOpacity(float opacity) {
        this.opacity = opacity;
    }

    public boolean isAspectRatioLock() {
        return aspectRatioLock;
    }

    public void setAspectRatioLock(boolean aspectRatioLock) {
        this.aspectRatioLock = aspectRatioLock;
    }

    public float getElementAspectRatio() {
        return elementAspectRatio;
    }

    public void setElementAspectRatio(float elementAspectRatio) {
        this.elementAspectRatio = elementAspectRatio;
    }


    @Override
    void doClassSpecificRender() {

    }

    @Override
    public void renderElement(int animationType) {

    }

    @Override
    public Node getCoreNode() {
        return null;
    }

    @Override
    void setupElement() {

    }

    public void setDurationSequence(float durationSequence){
        this.durationSequence = durationSequence;
    }
}
