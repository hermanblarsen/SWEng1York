package utilities;

/**
 * Created by habl on 25/02/2017.
 */
public class ImageHandler {

    //Dummy class for parsing
    protected int layer;
    protected boolean visibility;
    protected float xPosition;
    protected float yPosition;
    protected float xSize;
    protected float ySize;
    protected String path;
    protected String onClickAction;
    protected String onClickInfo;
    protected float opacity;
    protected boolean aspectRatioLock;
    protected float elementAspectRatio;

    public int getLayer() {
        return layer;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }

    public boolean isVisibility() {
        return visibility;
    }

    public void setVisibility(boolean visibility) {
        this.visibility = visibility;
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

    public String getOnClickAction() {
        return onClickAction;
    }

    public void setOnClickAction(String onClickAction) {
        this.onClickAction = onClickAction;
    }

    public String getOnClickInfo() {
        return onClickInfo;
    }

    public void setOnClickInfo(String onClickInfo) {
        this.onClickInfo = onClickInfo;
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
}
