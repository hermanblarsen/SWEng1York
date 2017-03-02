package utilities;

import javafx.scene.Node;
import javafx.scene.layout.Pane;

/**
 * Created by habl on 23/02/2017.
 */
public interface SlideElement  {
    //Empty interface for tagging our actual slide elements
    void renderElement(int animationType);
    Node getCoreNode();
    void setSlideCanvas(Pane slideCanvas);
    int getLayer();
    int getStartSequence();
}
