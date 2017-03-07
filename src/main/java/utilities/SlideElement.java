package utilities;

import javafx.scene.Node;
import javafx.scene.layout.Pane;

/**
 * Created by habl on 23/02/2017.
 */
public abstract class SlideElement  {

    public void renderElement(int animationType) {

    }

    public abstract Node getCoreNode();

    public void setSlideCanvas(Pane slideCanvas) {

    }

    public int getLayer(){

        return 0;
    }

    public int getStartSequence(){

        return 0;
    }

    public int getEndSequence() {

        return 0;
    }

    public void setVisibility(boolean visibility) {

    }
}
