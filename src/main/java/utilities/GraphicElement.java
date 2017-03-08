package utilities;

import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by habl on 26/02/2017.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class GraphicElement extends SlideElement {
    protected boolean aspectRatioLock;
    protected float elementAspectRatio;
    protected String lineColour;
    protected String fillColour;

    protected boolean isPolygon;

    //Polygon
    protected Polygon polygon;

    //Oval
    protected Oval oval;

    Logger logger = LoggerFactory.getLogger(GraphicElement.class);
    Pane slideCanvas;//The ParentPane of this element
    Pane wrapperCanvas;//There is a need to wrap the canvas that we draw to
    Canvas internalCanvas; //What we actually draw to

    public GraphicElement() {

    }

    @Override
    void doClassSpecificRender() {
        //TODO: Refresh canvas. Unsure if this deos this.
        wrapperCanvas.requestLayout();

        //Draw Polygons in here
        final GraphicsContext gc = internalCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, internalCanvas.getWidth(), internalCanvas.getHeight());

        gc.setFill(Color.BLACK);
        gc.setLineWidth(5);

        gc.setStroke(Color.BLUEVIOLET);
        gc.strokeOval(30, 60, 30, 30);
        gc.setStroke(Color.BLUE);
        gc.strokeOval(50, 60, 30, 30);
        gc.setStroke(Color.INDIANRED);
        gc.strokeOval(70, 60, 30, 30);
        // BIG BOOTY BITCHES
    }

    @Override
    public Node getCoreNode() {
        return wrapperCanvas;
    }

    @Override
    void setupElement() {
        // Create a wrapper Pane first
        wrapperCanvas = new Pane();

        internalCanvas = new Canvas(wrapperCanvas.getWidth(), wrapperCanvas.getHeight());
        wrapperCanvas.getChildren().add(internalCanvas);

        // Bind the width/height property to the wrapper Pane
        internalCanvas.widthProperty().bind(wrapperCanvas.widthProperty());
        internalCanvas.heightProperty().bind(wrapperCanvas.heightProperty());
        // redraw when resized
        internalCanvas.widthProperty().addListener(event -> renderElement(0));
        internalCanvas.heightProperty().addListener(event -> renderElement(0));

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

    public void setAspectRatioLock(boolean aspectRatioLock) {
        this.aspectRatioLock = aspectRatioLock;
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

    public void setLineColour(String lineColour) {
        this.lineColour = lineColour;
    }

    public String getFillColour() {
        return fillColour;
    }

    public void setFillColour(String fillColour) {
        this.fillColour = fillColour;
    }

    public Polygon getPolygon() {
        return polygon;
    }

    public void setPolygon(Polygon polygon) {
        this.polygon = polygon;
    }

    public Oval getOval() {
        return oval;
    }

    public void setOval(Oval oval) {
        this.oval = oval;
    }

    public boolean isPolygon() {
        return isPolygon;
    }

    public void setPolygon(boolean polygon) {
        isPolygon = polygon;
    }


}
