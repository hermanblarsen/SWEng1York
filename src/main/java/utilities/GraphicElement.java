package utilities;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;


/**
 * Created by habl on 26/02/2017.
 * Modified by Zain 11/03/2017.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class GraphicElement extends SlideElement {
    protected boolean aspectRatioLock;
    protected float elementAspectRatio;

    //Shape Properties:
    protected String lineColour;
    protected String fillColour;
    private static final double LINE_THICKNESS = 3;

    protected Shape graphicShape;

    private Pane wrapperPane;//Wrap the graphics within its own pane so that absolute positioning works properly.

    public GraphicElement() {

    }

    @Override
    void doClassSpecificRender() {

    }

    @Override
    public Node getCoreNode() {
        return wrapperPane;
    }

    @Override
    void setupElement() {
        wrapperPane = new Pane();

        graphicShape.setFill(parseRGBAString(fillColour));
        graphicShape.setStroke(parseRGBAString(lineColour));
        graphicShape.setStrokeWidth(LINE_THICKNESS);

        wrapperPane.getChildren().add(graphicShape);

        //TODO: Create Animations here, but move to setAnimation setter when XML implemented
        startAnimation = new Animation();
        startAnimation.setCoreNodeToAnimate(getCoreNode());
        startAnimation.setAnimationType(Animation.SIMPLE_APPEAR);
        endAnimation = new Animation();
        endAnimation.setCoreNodeToAnimate(getCoreNode());
        endAnimation.setAnimationType(Animation.SIMPLE_DISAPPEAR);
    }

    public boolean isAspectRatioLock() {
        return aspectRatioLock;
    }

    public GraphicElement setAspectRatioLock(boolean aspectRatioLock) {
        this.aspectRatioLock = aspectRatioLock;
        return this;
    }

    public float getElementAspectRatio() {
        return elementAspectRatio;
    }

    public void setElementAspectRatio(float elementAspectRatio) {
        this.elementAspectRatio = elementAspectRatio;
    }

    public String getLineColour() {
        return lineColour;
    }

    public GraphicElement setLineColour(String lineColour) {
        this.lineColour = lineColour;
        return this;
    }

    public String getFillColour() {
        return fillColour;
    }

    public GraphicElement setFillColour(String fillColour) {
        this.fillColour = fillColour;
        return this;
    }

    public GraphicElement setShape(Shape shape) {
        this.graphicShape = shape;
        return this;
    }

    public static Color parseRGBAString(String rgba){
       String rgb = rgba.substring(0, 6);
       String alphaString = rgba.substring(6);
       double alpha = Integer.parseInt(alphaString, 16)/255;
       return Color.web(rgb, alpha);
    }
}
