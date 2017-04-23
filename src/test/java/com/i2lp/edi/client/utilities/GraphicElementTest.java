package com.i2lp.edi.client.utilities;

import com.i2lp.edi.client.Animation.Animation;
import com.i2lp.edi.client.presentationElements.GraphicElement;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class GraphicElementTest extends Application {
    private GraphicElement elementUnderTest;
    Pane testPane =  new Pane();

    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void start(Stage primaryStage){
        //Setup the root border pane, controls on the left.
        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 1000, 1000);

        //Setup the element
        GraphicElement myPolygon = new GraphicElement();
        myPolygon.setLayer(1);
        myPolygon.setStartSequence(1);
        myPolygon.setEndSequence(2);
        myPolygon.setFillColour("#00FF00FF");
        myPolygon.setLineColour("#0000FFFF");
        myPolygon.polySetXPoints(new float[] {0.1f, 0.5f, 0.5f});
        myPolygon.polySetYPoints(new float[] {0.1f, 0.1f, 0.6f});
        myPolygon.setPolygon(true);
        myPolygon.setClosed(true);
        myPolygon.setSlideWidth(scene.getWidth());
        myPolygon.setSlideHeight(scene.getHeight());

        //Fill in for a presentaiton
        myPolygon.setSlideCanvas(root);
        myPolygon.renderElement(Animation.NO_ANIMATION);

        //Setup the element
        GraphicElement myOval = new GraphicElement();
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
        myOval.setSlideWidth(scene.getWidth());
        myOval.setSlideHeight(scene.getHeight());

        //Fill in for a presentaiton
        myOval.setSlideCanvas(root);
        myOval.renderElement(Animation.NO_ANIMATION);

        primaryStage.setTitle("Graphical Elements Test");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Test
    public void testParseRGBAString(){
        assertEquals(new Color(1, 0, 1, 0), GraphicElement.parseRGBAString("#FF00FF00"));
        assertEquals(new Color(0, 1, 0, 1), GraphicElement.parseRGBAString("#00ff00FF"));
        assertEquals(new Color(0, 1, 0, 127f/255f), GraphicElement.parseRGBAString("#00FF007F"));
    }
}