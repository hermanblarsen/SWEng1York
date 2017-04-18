package com.i2lp.edi.client.Animation;


import javafx.scene.Node;
import javafx.util.Duration;
/**
 * Created by amriksadhra on 02/03/2017.
 */

public abstract class Animation {
    //Logger logger = LoggerFactory.getLogger(this.getClass());

    //Animation Appearance Types
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

//            case SVG_MOVEMENT_TEST://Will be moved to a different class for complex animation.
//                final Path path = new Path();
//                path.getElements().add(new MoveTo(20, 20));
//                path.getElements().add(new CubicCurveTo(30, 10, 380, 120, 200, 120));
//                path.getElements().add(new CubicCurveTo(200, 1120, 110, 240, 380, 240));
//                path.setOpacity(0.5);
//                final SVGPath path = new SVGPath();
//                path.setContent("M10 10 L100 100");
//                path.setStroke(Color.BLACK);
//
//
//                //group.getChildren().add(path);
//                //group.getChildren().add(circle);
//                final PathTransition pathTransition = new PathTransition();
//
//                pathTransition.setDuration(Duration.seconds(1.0));
//                pathTransition.setDelay(Duration.seconds(.5));
//                pathTransition.setPath(path);
//                pathTransition.setNode(getCoreNodeToAnimate());
//                pathTransition.setOrientation(PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT);
//                pathTransition.setCycleCount(Timeline.INDEFINITE);
//                //pathTransition.setAutoReverse(true);
//                pathTransition.play();


    public Node getCoreNodeToAnimate() {
        return coreNodeToAnimate;
    }

    public void setCoreNodeToAnimate(Node coreNodeToAnimate) {
        this.coreNodeToAnimate = coreNodeToAnimate;
    }


}
