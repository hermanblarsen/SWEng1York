package com.i2lp.edi.client.presentationElements;

import javafx.event.Event;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;


/**
 * Created by Kacper on 2017-04-30.
 */
public class DrawPane extends Pane {

    private boolean isActive;
    private boolean isEraserMode;
    private Logger logger = LoggerFactory.getLogger(DrawPane.class);
    private Canvas canvas;
    private GraphicsContext graphicsContext;
    private WritableImage drawing;
    private final StackPane parentPane;
    private double eraserSize = 5;
    private boolean newPathStarted = false;
    private ArrayList<Node> mouseEnteredArrayList = new ArrayList<>();

    public DrawPane(StackPane parent) {
        this.parentPane = parent;
        canvas = new Canvas(1, 1);
        getChildren().add(canvas);
        saveCanvasToImage();

        canvas.widthProperty().addListener(observable -> redraw());
        canvas.heightProperty().addListener(observable -> redraw());

        graphicsContext = canvas.getGraphicsContext2D();

        addEventFilter(MouseEvent.ANY, event -> {
            if(!isActive) {
                Node target = findEventTarget(event, (Parent) parentPane.getChildren().get(0));
                if(target != null) {
                    Event.fireEvent(target, event.copyFor(event.getSource(), target));

                    //Emulate MOUSE_ENTERED and MOUSE_EXITED events
                    if(event.getEventType().equals(MouseEvent.MOUSE_MOVED)) {
                        if(!mouseEnteredArrayList.contains(target)) {
                            addToMouseEnteredList(target, event);
                        }

                        removeChildrenFromMouseEnteredList(target, event);

                    }

                    event.consume();
                }
            }
        });

        addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            if(isActive) {
                graphicsContext.beginPath();
                graphicsContext.moveTo(event.getX(), event.getY());
                newPathStarted = true;
                if(!isEraserMode())
                    graphicsContext.fillRect(event.getX() - graphicsContext.getLineWidth()/2, event.getY() - graphicsContext.getLineWidth()/2, graphicsContext.getLineWidth(), graphicsContext.getLineWidth());
            }
        });

        addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            if(isActive) {
                if(!isEraserMode) {
                    if(!newPathStarted) { //Hack for when MOUSE_PRESSED wasn't detected first - happens sometimes for some reason
                        graphicsContext.beginPath();
                        graphicsContext.moveTo(event.getX(), event.getY());
                        newPathStarted = true;
                    }
                    graphicsContext.lineTo(event.getX(), event.getY());
                    graphicsContext.stroke();
                } else {
                    graphicsContext.clearRect(event.getX(), event.getY(), eraserSize, eraserSize);
                }
            }
        });

        addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
            if(isActive) {
                saveCanvasToImage();
                newPathStarted = false;
                Slide slide = (Slide) parentPane.getChildren().get(0);
                slide.addSlideDrawing(drawing);
            }
        });
    }

    private void addToMouseEnteredList(Node node, MouseEvent event) {
        if(!mouseEnteredArrayList.contains(node)) {
            mouseEnteredArrayList.add(node);
            MouseEvent emulatedEnteredEvent = event.copyFor(event.getSource(), node, MouseEvent.MOUSE_ENTERED);
            Event.fireEvent(node, emulatedEnteredEvent);
        }

        if(node.getParent() != node.getScene().getRoot())
            addToMouseEnteredList(node.getParent(), event);
    }

    private void removeChildrenFromMouseEnteredList(Node node, MouseEvent event) {
        Parent parent = null;

        try {
            parent = (Parent) node;
        } catch(ClassCastException e) {
            //The exception is thrown when the node is not a parent. Do nothing.
        }

        if(parent != null) {
            for(Node child : parent.getChildrenUnmodifiable()) {
                if(mouseEnteredArrayList.contains(child)) {
                    MouseEvent emulatedExitedEvent = event.copyFor(event.getSource(), child, MouseEvent.MOUSE_EXITED);
                    Event.fireEvent(child, emulatedExitedEvent);
                    mouseEnteredArrayList.remove(child);
                    removeChildrenFromMouseEnteredList(child, event);
                }
            }
        }
    }

    private Node findEventTarget(MouseEvent event, Parent parent) {
        logger.debug("Searching for event target in " + parent.toString());
        Node foundTarget = parent;
        if(parent.getChildrenUnmodifiable().size() != 0) {
            for(Node child : parent.getChildrenUnmodifiable()) {
                Bounds boundsInScene = child.localToScene(child.getBoundsInLocal());
                if(boundsInScene.contains(event.getSceneX(), event.getSceneY())) {
                    if(child instanceof Parent) {
                        foundTarget = findEventTarget(event, (Parent) child);
                    } else {
                        foundTarget = child;
                    }
                }
            }
        }
        return foundTarget;
    }

    private void redraw() {
        graphicsContext.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        graphicsContext.drawImage(drawing, 0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private void saveCanvasToImage() {
        drawing = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
        SnapshotParameters snapshotParameters = new SnapshotParameters();
        snapshotParameters.setFill(Color.TRANSPARENT);
        canvas.snapshot(snapshotParameters, drawing);
    }

    @Override //from http://stackoverflow.com/questions/31761361/automatically-resize-canvas-to-fill-the-enclosing-parent
    protected void layoutChildren() {
        final double x = snappedLeftInset();
        final double y = snappedTopInset();
        final double w = snapSize(getWidth()) - x - snappedRightInset();
        final double h = snapSize(getHeight()) - y - snappedBottomInset();
        canvas.setLayoutX(x);
        canvas.setLayoutY(y);
        canvas.setWidth(w);
        canvas.setHeight(h);
    }

    public void setBrushWidth(double width) {
        graphicsContext.setLineWidth(width);
    }

    public double getBrushWidth() { return graphicsContext.getLineWidth(); }

    public void setBrushColor(Color color) {
        graphicsContext.setStroke(color);
        graphicsContext.setFill(color);
    }

    public Color getBrushColor() { return (Color) graphicsContext.getStroke(); }

    public void clear() {
        graphicsContext.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        saveCanvasToImage();
    }

    public WritableImage getSlideDrawing() { return drawing; }

    public void setSlideDrawing(WritableImage drawing) {
        graphicsContext.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        this.drawing = drawing;
        redraw();
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public Canvas getCanvas() { return canvas; }

    public void setEraserMode(boolean eraserMode) { this.isEraserMode = eraserMode; }

    public boolean isEraserMode() { return isEraserMode; }

    public void setEraserSize(double size) { this.eraserSize = size; }

    public double getEraserSize() { return eraserSize; }
}
