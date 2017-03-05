package utilities;

import javafx.scene.Node;
import javafx.scene.layout.Pane;

/**
 * Created by habl on 23/02/2017.
 */
public interface SlideElement  {
    //Empty interface for tagging our actual slide elements
    //TODO whoever put this here, the interface is supposed to be fully empty, or it might cause problems with externally produced Objects
    //TODO find a way to implement this properly, maybe by casting or something somewhere else?
    void renderElement(int animationType);
    Node getCoreNode();
    void setSlideCanvas(Pane slideCanvas);
    int getLayer();
    int getStartSequence();
    int getEndSequence();
    void setVisibility(boolean visibility);
}
