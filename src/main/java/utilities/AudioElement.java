package utilities;

import javafx.scene.Node;
import javafx.scene.layout.Pane;

/**
 * Created by habl on 25/02/2017.
 */
public class AudioElement extends AudioHandler implements SlideElement {
    protected int elementID;
    protected int startSequence;
    protected int endSequence;
    protected float durationSequence;

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

    @Override
    public int getEndSequence() {
        return endSequence;
    }

    @Override
    public void setVisibility(boolean visibility) {

    }

    public int getElementID() {
        return elementID;
    }

    public void setElementID(int elementID) {
        this.elementID = elementID;
    }

    public void setStartSequence(int startSequence) {
        this.startSequence = startSequence;
    }

    public void setEndSequence(int endSequence) {
        this.endSequence = endSequence;
    }

    public float getDurationSequence() {
        return durationSequence;
    }

    public void setDurationSequence(float durationSequence) {
        this.durationSequence = durationSequence;
    }
}
