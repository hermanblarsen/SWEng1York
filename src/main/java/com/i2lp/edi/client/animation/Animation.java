package com.i2lp.edi.client.animation;


import javafx.scene.Node;
import javafx.util.Duration;
import sun.plugin.dom.exception.InvalidStateException;

/**
 * Created by amriksadhra on 02/03/2017.
 */

public abstract class Animation {
    //Logger logger = LoggerFactory.getLogger(this.getClass());

    //animation Appearance Types
    public final static int NO_ANIMATION = 0;
    public final static int ENTRY_ANIMATION = 1;
    public final static int EXIT_ANIMATION = 2;

    Node coreNodeToAnimate;
    protected Duration duration; //The duration of any transition

    public  Animation(){}
    /*
     * Play animation
     */
    public abstract void play();


    public Node getCoreNodeToAnimate() {
        return coreNodeToAnimate;
    }

    public void setCoreNodeToAnimate(Node coreNodeToAnimate) {
        this.coreNodeToAnimate = coreNodeToAnimate;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public Duration getDuration() {
        return duration;
    }
}
