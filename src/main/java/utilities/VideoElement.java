package utilities;

import javafx.scene.Node;
import javafx.scene.layout.Pane;

/**
 * Created by habl on 26/02/2017.
 */
public class VideoElement implements SlideElement{
    protected int elementID;
    protected int layer;
    protected boolean visibility;
    protected int startSequence;
    protected int endSequence;
    protected float duration;
    protected float xPosition;
    protected float yPosition;
    protected float xSize;
    protected float ySize;
    protected String path;
    protected String onClickAction;
    protected String onClickInfo;
    protected boolean loop;
    protected boolean aspectRatioLock;
    protected float elementAspectRatio;
    protected boolean autoplay;
    protected int startTime;
    protected int endTime;

    @Override
    public void renderElement(int animationType) {

    }

    @Override
    public Node getCoreNode() {
        return null;
    }

    @Override
    public void setSlideCanvas(Pane slideCanvas) {

    }

    @Override
    public int getLayer() {
        return 0;
    }

    @Override
    public int getStartSequence() {
        return 0;
    }
}
