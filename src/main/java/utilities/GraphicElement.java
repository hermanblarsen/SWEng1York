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
public class GraphicElement implements SlideElement {
    protected int elementID;
    protected int layer;
    protected boolean visibility;
    protected int startSequence;
    protected int endSequence;
    protected float duration;
    protected String onClickAction;
    protected String onClickInfo;
    protected boolean aspectRatioLock;
    protected float elementAspectRatio;
    protected String lineColour;
    protected String fillColour;

    protected boolean isPolygon;

    //Polygon
    protected Polygon polygon;

    //Oval
    protected Oval oval;

    protected Animation startAnimation, endAnimation;

    Logger logger = LoggerFactory.getLogger(GraphicElement.class);
    Pane slideCanvas;//The ParentPane of this element
    Pane wrapperCanvas;//There is a need to wrap the canvas that we draw to
    Canvas internalCanvas; //What we actually draw to

    public GraphicElement() {

    }

    public void setupGraphicElement() { //TODO moved from constructor, so is no longer called intrinsically
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
        endAnimation.setAnimationType(Animation.MOVEMENT_TEST);
    }

    public int getStartSequence() {
        return startSequence;
    }

    @Override
    public int getEndSequence() {
        return endSequence;
    }

    @Override
    public void setVisibility(boolean visibility) {
        this.visibility = visibility;
    }

    public void setStartSequence(int startSequence) {
        this.startSequence = startSequence;
    }

    @Override
    public void renderElement(int animationType) {
        //TODO: Trigger redraw of Canvas. Unsure if this achieves this.
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

        //TODO: REfactor to avoid duplicated code.
        switch (animationType) {
            case Animation.NO_ANIMATION: //No animation (click)
                logger.info("No animation");
                break;
            case Animation.ENTRY_ANIMATION: //Entry Animation (playback)
                if (startAnimation != null) {//Animation Exists as StartSequence Present
                    startAnimation.play();
                    logger.info("Entry animation playing");
                }
                break;
            case Animation.EXIT_ANIMATION: //Exit Animation (playback)
                if (endAnimation != null) {//Animation Exists as EndSequence Present
                    endAnimation.play();
                    logger.info("Exit animation playing");
                }
                break;
        }
    }

    @Override
    public Node getCoreNode() {
        return wrapperCanvas;
    }

    @Override
    public void setSlideCanvas(Pane slideCanvas) {
        setupGraphicElement();
        this.slideCanvas = slideCanvas;

        //Add WrapperCanvas Element to the Pane
        if (wrapperCanvas == null) {
            logger.error("Tried to set slide internalCanvas before GraphicElement constructor was called!");
        } else {
            //Canvas is the corenode
            slideCanvas.getChildren().add(wrapperCanvas);
        }
    }

    @Override
    public int getLayer() {
        return layer;
    }

    public int getElementID() {
        return elementID;
    }

    public void setElementID(int elementID) {
        this.elementID = elementID;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }

    public boolean isVisibility() {
        return visibility;
    }

    public void setEndSequence(int endSequence) {
        this.endSequence = endSequence;
    }

    public float getDuration() {
        return duration;
    }

    public void setDuration(float duration) {
        this.duration = duration;
    }

    public String getOnClickAction() {
        return onClickAction;
    }

    public void setOnClickAction(String onClickAction) {
        this.onClickAction = onClickAction;
    }

    public String getOnClickInfo() {
        return onClickInfo;
    }

    public void setOnClickInfo(String onClickInfo) {
        this.onClickInfo = onClickInfo;
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
