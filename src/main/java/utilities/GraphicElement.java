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
public class GraphicElement implements SlideElement  {
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
    protected Polygon polygon;
    protected Oval oval;

    Logger logger = LoggerFactory.getLogger(GraphicElement.class);
    Pane slideCanvas;//The ParentPane of this element
    Pane wrapperCanvas;//There is a need to wrap the canvas that we draw to
    Canvas internalCanvas; //What we actually draw to

    public GraphicElement(){
        // Create a wrapper Pane first
        wrapperCanvas = new Pane();

        internalCanvas = new Canvas(wrapperCanvas.getWidth(), wrapperCanvas.getHeight());
        wrapperCanvas.getChildren().add(internalCanvas);

        // Bind the width/height property to the wrapper Pane
        internalCanvas.widthProperty().bind(wrapperCanvas.widthProperty());
        internalCanvas.heightProperty().bind(wrapperCanvas.heightProperty());
        // redraw when resized
        internalCanvas.widthProperty().addListener(event -> renderElement());
        internalCanvas.heightProperty().addListener(event -> renderElement());
    }

    @Override
    public void renderElement() {
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
    }

    @Override
    public Node getCoreNode() {
        return wrapperCanvas;
    }

    @Override
    public void setSlideCanvas(Pane slideCanvas) {
        this.slideCanvas = slideCanvas;

        //Canvas is the corenode
        slideCanvas.getChildren().add(wrapperCanvas);
    }
}
