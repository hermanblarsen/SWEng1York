package utilities;

/**
 * Created by habl on 26/02/2017.
 */
public class GraphicElement implements SlideElement{
    protected int elementID;
    protected int layer;
    protected boolean visibility;
    protected int startSequence;
    protected int endSequence;
    protected float duration;
    protected String onClickAction;
    protected String onClickInfo;
    protected boolean aspectRatioLock;
    protected float elementAspectRatio;
    protected String lineColour;
    protected String fillColour;
    protected Polygon polygon;
    protected Oval oval;
}
