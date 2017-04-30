package com.i2lp.edi.client.presentationElements;

import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by Kacper on 2017-04-30.
 */
public class DrawPane extends Pane {

    private boolean active;
    private Logger logger = LoggerFactory.getLogger(DrawPane.class);
    private Canvas canvas;
    private GraphicsContext graphicsContext;
    private WritableImage drawing;

    public DrawPane() {
        canvas = new Canvas(1, 1);
        getChildren().add(canvas);
        saveCanvasToImage();

        canvas.widthProperty().addListener(observable -> redraw());
        canvas.heightProperty().addListener(observable -> redraw());

        graphicsContext = canvas.getGraphicsContext2D();

        addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            if(active) {
                graphicsContext.beginPath();
                graphicsContext.moveTo(event.getX(), event.getY());
            }
        });

        addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            if(active) {
                graphicsContext.lineTo(event.getX(), event.getY());
                graphicsContext.stroke();
                saveCanvasToImage();
            }
        });
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

    public void setBrushPaint(Paint paint) {
        graphicsContext.setStroke(paint);
    }

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
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
