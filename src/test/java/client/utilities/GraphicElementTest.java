package client.utilities;

import client.presentationElements.GraphicElement;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;


public class GraphicElementTest {
    private GraphicElement elementUnderTest;
    Pane testPane =  new Pane();

    @Before
    public void setUp(){
        GraphicElement myGraphicElement = new GraphicElement();
        myGraphicElement.setLayer(1);
        myGraphicElement.setStartSequence(1);
        myGraphicElement.setEndSequence(2);
        myGraphicElement.setFillColour("00FF00FF");
        myGraphicElement.setLineColour("0000FFFF");
        myGraphicElement.setShape(new OvalBuilder(
                        100.0f,
                        100.0f,
                        30.0f,
                        30.0f,
                        0
                ).build()
        );
        myGraphicElement.setSlideCanvas(testPane);
    }

    @Test
    public void testParseRGBAString(){
        assertEquals(new Color(1, 0, 1, 0), GraphicElement.parseRGBAString("FF00FF00"));
        assertEquals(new Color(0, 1, 0, 1), GraphicElement.parseRGBAString("00ff00FF"));
        assertEquals(new Color(0, 1, 0, 127/255), GraphicElement.parseRGBAString("00FF007F"));
    }
}