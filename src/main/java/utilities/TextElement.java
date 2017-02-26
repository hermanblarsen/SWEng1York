package utilities;

/**
 * Created by habl on 26/02/2017.
 */
public class TextElement implements SlideElement{
    protected int elementID;
    protected int layer;
    protected boolean visibility;
    protected int startSequence;
    protected int endSequence;
    protected float duration;
    protected String textContent;
    protected String textFilepath;
    protected String textContentReference;
    protected float yPosition;
    protected float xSize;
    protected float ySize;
    protected String font;
    protected int fontSize;
    protected String fontColour;
    protected String bgColour;
    protected String borderColour;
    protected String onClickAction;
    protected String onClickInfo;
    protected boolean aspectRatioLock;
    protected float elementAspectRatio;
}
