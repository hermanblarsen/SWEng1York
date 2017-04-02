package client.presentationElements;

import client.utilities.OvalBuilder;
import client.utilities.PolygonBuilder;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
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
    protected OvalBuilder oval;
    protected PolygonBuilder polygon;
    protected boolean isPolygon;

    private Pane wrapperPane;//Wrap the graphics within its own pane so that absolute positioning works properly.

    public GraphicElement() {

    }

    @Override
    public void doClassSpecificRender() {

    }

    @Override
    public Node getCoreNode() {
        return wrapperPane;
    }

    @Override
    public void setupElement() {
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

    @Override
    public void destroyElement() {

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

    public GraphicElement setShape(Shape shape) { //TODO Why does this return the GraphicsElement? -Herman
        this.graphicShape = shape;
        return this;
    }

    public void setOval(OvalBuilder oval) {
        this.oval = oval;
        isPolygon = false;
        setShape(oval.build());
    }

    public void setPolygon(PolygonBuilder polygon) {
        this.polygon = polygon;
        isPolygon = true;
        setShape(polygon.build());
    }

    public Shape getGraphicShape() {
        return graphicShape;
    }

    public static Color parseRGBAString(String rgba){
       String rgb = rgba.substring(0, 6);
       String alphaString = rgba.substring(6);
       double alpha = Integer.parseInt(alphaString, 16)/255;
       return Color.web(rgb, alpha);
    }

    public OvalBuilder getOval() {
        return oval;
    }

    public PolygonBuilder getPolygon() {
        return polygon;
    }
}
