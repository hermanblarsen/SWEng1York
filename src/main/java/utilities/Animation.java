package utilities;


import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 * Created by amriksadhra on 02/03/2017.
 */
public class Animation {
    //Animation Appearance Types
    final static int NO_ANIMATION = 0;
    final static int ENTRY_ANIMATION = 1;
    final static int EXIT_ANIMATION = 2;
    //Types of Animation
    final static int SIMPLE_APPEAR = 0;
    final static int SIMPLE_DISAPPEAR = 1;
    final static int MOVEMENT_TEST = 2;

    public int getAnimationType() {
        return animationType;
    }

    public void setAnimationType(int animationType) {
        this.animationType = animationType;
    }

    //Storage for Type
    private int animationType = 0;

    Node coreNodeToAnimate;
    //[VARIABLES TO STORE ANIMATION PATH/DETAILS NEEDED HERE]


    public Animation() {

    }

    /*
     * Play animation
     */
    public void play() {
        switch (animationType) {
            case SIMPLE_APPEAR://Appear
                getCoreNodeToAnimate().setVisible(true);
                break;

            case SIMPLE_DISAPPEAR://Hide
                getCoreNodeToAnimate().setVisible(false);
                break;

            case MOVEMENT_TEST://Move downwards every 250ms
                //Create new timer and schedule increment of first nodes Y position: Test animation
                Timeline timeline = new Timeline(new KeyFrame(
                        Duration.millis(250),
                        ae -> getCoreNodeToAnimate().setTranslateY(getCoreNodeToAnimate().getTranslateY() + 1)));
                timeline.setCycleCount(30);
                timeline.play();
                break;
        }
    }

    public Node getCoreNodeToAnimate() {
        return coreNodeToAnimate;
    }

    public void setCoreNodeToAnimate(Node coreNodeToAnimate) {
        this.coreNodeToAnimate = coreNodeToAnimate;
    }


}
