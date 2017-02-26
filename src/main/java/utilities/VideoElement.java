package utilities;

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
}
