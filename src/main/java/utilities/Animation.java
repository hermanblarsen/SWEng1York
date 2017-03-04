package utilities;

import javax.xml.soap.Node;

/**
 * Created by amriksadhra on 02/03/2017.
 */
public class Animation {
    //Animation Types
    final static int NO_ANIMATION = 0;
    final static int ENTRY_ANIMATION = 1;
    final static int EXIT_ANIMATION = 2;

    Node coreNodeToAnimate;
    //[VARIABLES TO STORE ANIMATION PATH/DETAILS NEEDED HERE]


    public Animation(){

    }

    /*
     * Play animation
     */
    public void play(){

    }

    public Node getCoreNodeToAnimate() {
        return coreNodeToAnimate;
    }

    public void setCoreNodeToAnimate(Node coreNodeToAnimate) {
        this.coreNodeToAnimate = coreNodeToAnimate;
    }



}
