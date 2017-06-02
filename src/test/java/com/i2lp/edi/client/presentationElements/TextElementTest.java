package com.i2lp.edi.client.presentationElements;

import com.i2lp.edi.client.animation.Animation;
import com.i2lp.edi.client.managers.EdiManager;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import static com.i2lp.edi.client.Constants.IS_CIRCLE_BUILD;
import static org.junit.Assert.*;

/**
 * Created by Luke on 25/05/2017.
 */
public class TextElementTest extends ApplicationTest {
    private TextElement myTextElement;
    private Pane textPane;

    @Override
    public void start(Stage stage) throws Exception {
        if (IS_CIRCLE_BUILD) {
            System.out.println("Skipping test requiring graphics on circle.ci (CI server is headless)");
            return;
        }

        textPane = new BorderPane();
        Scene scene = new Scene(textPane, 600, 600);

        myTextElement = new TextElement();
        myTextElement.setPresentation(new Presentation());
        myTextElement.setEdiManager(new EdiManager());
        myTextElement.setTextContent("Test Text");

        myTextElement.setFont("Arial");
        myTextElement.setFontSize(12);
        myTextElement.setFontColour("#AF4567");
        myTextElement.setBgColour("#000000");
        myTextElement.setBorderColour("#000000");
        myTextElement.setBorderSize(20);
        myTextElement.setHasBorder(false);

        myTextElement.setSlideWidth(scene.getWidth());
        myTextElement.setSlideHeight(scene.getHeight());
        myTextElement.setSlideCanvas(textPane);
        myTextElement.renderElement(Animation.NO_ANIMATION);

        stage.setScene(scene);
        stage.show();
        stage.toFront();
    }

    @Before
    public void setUp() {
        //Ignores the test if the build is run from circle (headless) environment
        Assume.assumeTrue(!IS_CIRCLE_BUILD);
    }

    @Test
    public void testCreation() {
        assertEquals(12, myTextElement.getFontSize());
        assertEquals("Arial", myTextElement.getFont());
        assertEquals("#AF4567", myTextElement.getFontColour());
        assertEquals("#000000", myTextElement.getBgColour());
        assertEquals("#000000", myTextElement.getBorderColour());
        assertEquals(20, myTextElement.getBorderSize());
        assertFalse(myTextElement.getHasBorder());
    }

    @Test
    public void testFontSize() {
        myTextElement.setFontSize(20);
        assertEquals(20, myTextElement.getFontSize());
    }

    @Test
    public void testFont() {
        myTextElement.setFont("Serif");
        assertEquals("Serif", myTextElement.getFont());
    }

    @Test
    public void testFontColour() {
        myTextElement.setFontColour("#000000");
        assertEquals("#000000", myTextElement.getFontColour());
    }

    @Test
    public void testBgColour() {
        myTextElement.setBgColour("#AF4567");
        assertEquals("#AF4567", myTextElement.getBgColour());
    }

    @Test
    public void testBorderColour() {
        myTextElement.setBorderColour("#AF4567");
        assertEquals("#AF4567", myTextElement.getBorderColour());
    }

    @Test
    public void testBorderSize() {
        myTextElement.setBorderSize(50);
        assertEquals(50, myTextElement.getBorderSize());
    }

    @Test
    public void testHasBorder() {
        myTextElement.setHasBorder(true);
        assertTrue(myTextElement.getHasBorder());
    }


}
