package com.i2lp.edi.client.presentationElements;

import com.i2lp.edi.client.animation.Animation;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.junit.Assume;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import static com.i2lp.edi.client.Constants.IS_CIRCLE_BUILD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class GraphicElementTest extends ApplicationTest {
    private GraphicElement elementUnderTest;
    private Pane testPane = new Pane();
    private GraphicElement myPolygon;
    private GraphicElement myOval;

    @Override
    public void start(Stage primaryStage) {
        if (IS_CIRCLE_BUILD) {
            System.out.println("Skipping test requiring graphics on circle.ci (CI server is headless)");

            setUpElements();

            return;
        }

        setUpElements();

        //Setup the root border pane, controls on the left.
        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 1000, 1000);

        myPolygon.setSlideWidth(scene.getWidth());
        myPolygon.setSlideHeight(scene.getHeight());
        //Fill in for a presentaiton
        myPolygon.setSlideCanvas(root);
        myPolygon.renderElement(Animation.NO_ANIMATION);

        //Fill in for a presentaiton
        myOval.setSlideCanvas(root);
        myOval.renderElement(Animation.NO_ANIMATION);

        myOval.setSlideWidth(scene.getWidth());
        myOval.setSlideHeight(scene.getHeight());

        primaryStage.setTitle("Graphical Elements Test");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void setUpElements() {
        //Setup the element
        myPolygon = new GraphicElement();
        myPolygon.setLayer(1);
        myPolygon.setStartSequence(1);
        myPolygon.setEndSequence(2);
        myPolygon.setFillColour("#00FF00FF");
        myPolygon.setLineColour("#0000FFFF");
        myPolygon.polySetXPoints(new float[]{0.1f, 0.5f, 0.5f});
        myPolygon.polySetYPoints(new float[]{0.1f, 0.1f, 0.6f});
        myPolygon.setPolygon(true);
        myPolygon.setClosed(true);

        //Setup the element
        myOval = new GraphicElement();
        myOval.setLayer(1);
        myOval.setStartSequence(1);
        myOval.setEndSequence(2);
        myOval.setFillColour("#FF00008F");
        myOval.setLineColour("#00FF00FF");
        myOval.setOvalYPosition(0.4f);
        myOval.setOvalXPosition(0.5f);
        myOval.setrHorizontal(0.1f);
        myOval.setrVertical(0.3f);
        myOval.setRotation(45);
        myOval.setPolygon(false);
    }

    @Test
    public void testParseRGBAString() {
        //Ignores the test if the build is run from circle (headless) environment
//        Assume.assumeTrue(!IS_CIRCLE_BUILD); //THis specific test doesn't need the graphics.

        assertEquals(new Color(1, 0, 1, 0), GraphicElement.parseRGBAString("#FF00FF00"));
        assertEquals(new Color(0, 1, 0, 1), GraphicElement.parseRGBAString("#00ff00FF"));
        assertEquals(new Color(0, 1, 0, 127f / 255f), GraphicElement.parseRGBAString("#00FF007F"));
    }

    @Test
    public void testOvalCreation() {
        assertEquals(1, myOval.getLayer());
        assertEquals(1, myOval.getStartSequence());
        assertEquals(2, myOval.getEndSequence());
        assertEquals("#FF00008F", myOval.getFillColour());
        assertEquals("#00FF00FF", myOval.getLineColour());
        assertEquals(0.4f, myOval.getOvalYPosition(), 0);
        assertEquals(0.5f, myOval.getOvalXPosition(), 0);
        assertEquals(0.1f, myOval.getrHorizontal(), 0);
        assertEquals(0.3f, myOval.getrVertical(), 0);
        assertEquals(45, myOval.getRotation(), 0);
        assertEquals(false, myOval.isPolygon());
    }

    @Test
    public void testPolygonCreation() {
        assertEquals(myPolygon.getLayer(), 1);
        assertEquals(myPolygon.getStartSequence(), 1);
        assertEquals(myPolygon.getEndSequence(), 2);
        assertEquals(myPolygon.getFillColour(), "#00FF00FF");
        assertEquals(myPolygon.getLineColour(), "#0000FFFF");
        assertEquals(myPolygon.getPolyXPositions()[0], 0.1f, 0);
        assertEquals(myPolygon.getPolyXPositions()[1], 0.5f, 0);
        assertEquals(myPolygon.getPolyXPositions()[2], 0.5f, 0);
        assertEquals(myPolygon.getPolyYPositions()[0], 0.1f, 0);
        assertEquals(myPolygon.getPolyYPositions()[1], 0.1f, 0);
        assertEquals(myPolygon.getPolyYPositions()[2], 0.6f, 0);
        assertEquals(myPolygon.isPolygon(), true);
        assertEquals(myPolygon.isClosed(), true);
    }
}