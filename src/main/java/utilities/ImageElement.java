package utilities;

import javafx.scene.Node;
import javafx.scene.layout.Pane;

/**
 * Created by habl on 25/02/2017.
 */
public class ImageElement extends ImageHandler implements SlideElement {
    protected int elementID;
    protected int startSequence;
    protected int endSequence;
    protected float durationSequence;

    @Override
    public void renderElement() {

    }

    @Override
    public Node getCoreNode() {
        return null;
    }

    @Override
    public void setSlideCanvas(Pane slideCanvas) {

    }
}
