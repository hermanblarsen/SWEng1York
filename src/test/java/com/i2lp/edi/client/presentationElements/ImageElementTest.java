package com.i2lp.edi.client.presentationElements;

import com.i2lp.edi.client.animation.Animation;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import java.io.FileNotFoundException;

import static org.junit.Assert.*;

/**
 * Created by Zain on 07/05/2017.
 */
public class ImageElementTest extends ApplicationTest {
    private ImageElement myImageElementFile;
    private ImageElement myImageElementHttp;

    @Override
    public void start(Stage primaryStage){
        //Setup the root border pane, controls on the left.
        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 1000, 1000);

        //Setup the Element
        try {
            myImageElementFile = new ImageElement();
            myImageElementFile.setLayer(1);
            myImageElementFile.setVisibility(true);
            myImageElementFile.setStartSequence(1);
            myImageElementFile.setEndSequence(2);
            myImageElementFile.setPath("projectResources/logos/ediLogo400x400.png");
            myImageElementFile.aspectRatioLock(false);

            myImageElementFile.setPosX(0.7f);
            myImageElementFile.setPosY(0f);
            myImageElementFile.setHeight(0.5f);
            myImageElementFile.setWidth(0.5f);
            myImageElementFile.setRotation(45);
            myImageElementFile.setBorder(true);
            myImageElementFile.setBorderWidth(0.05f);
            myImageElementFile.setBorderColour("#000000FF");

            myImageElementHttp = new ImageElement();
            myImageElementHttp.setLayer(1);
            myImageElementHttp.setStartSequence(1);
            myImageElementHttp.setEndSequence(2);
            myImageElementHttp.setPath("https://www.google.co.uk/images/branding/googlelogo/2x/googlelogo_color_272x92dp.png");
            myImageElementHttp.aspectRatioLock(true);
            myImageElementHttp.setVisibility(true);

            myImageElementHttp.setPosX(0.1f);
            myImageElementHttp.setPosY(0.1f);
            myImageElementHttp.setWidth(0.4f);

            myImageElementHttp.setSlideWidth(scene.getWidth());
            myImageElementHttp.setSlideHeight(scene.getHeight());
            myImageElementFile.setSlideWidth(scene.getWidth());
            myImageElementFile.setSlideHeight(scene.getHeight());

            //Do the functions which would normally be done by a presentation manager
            myImageElementHttp.setSlideCanvas(root);
            myImageElementFile.setSlideCanvas(root);
            myImageElementHttp.renderElement(Animation.ENTRY_ANIMATION);
            myImageElementFile.renderElement(Animation.ENTRY_ANIMATION);
        } catch (FileNotFoundException fnfe){
            fnfe.printStackTrace();
        }

        primaryStage.setTitle("Image Element Test");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Test
    public void testImageCreationFromFile() {
        assertEquals("projectResources/logos/ediLogo400x400.png", myImageElementFile.getPath());
        assertEquals(false, myImageElementFile.aspectRatioLocked);
        assertEquals(0.7f, myImageElementFile.getPosX(), 0);
        assertEquals(0f, myImageElementFile.getPosY(), 0);
        assertEquals(0.5f, myImageElementFile.getHeight(), 0);
        assertEquals(0.5f, myImageElementFile.getWidth(), 0);
        assertEquals(45, myImageElementFile.getRotation(), 0);
        assertEquals(true, myImageElementFile.isBorder());
        assertEquals(0.05f, myImageElementFile.getBorderWidth(), 0);
        assertEquals("#000000FF", myImageElementFile.getBorderColour());
    }

    @Test
    public void testImageCreationFromHttp() {
        assertEquals("https://www.google.co.uk/images/branding/googlelogo/2x/googlelogo_color_272x92dp.png", myImageElementHttp.getPath());
        assertEquals(true, myImageElementHttp.aspectRatioLocked());
        assertEquals(0.1f, myImageElementHttp.getPosX(), 0);
        assertEquals(0.1f, myImageElementHttp.getPosY(), 0);
        assertEquals(0.4f, myImageElementHttp.getWidth(), 0);
    }
}
