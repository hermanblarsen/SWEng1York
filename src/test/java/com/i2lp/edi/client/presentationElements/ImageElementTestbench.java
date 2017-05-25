package com.i2lp.edi.client.presentationElements;

import com.i2lp.edi.client.animation.Animation;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.FileNotFoundException;

/**
 * Created by Zain on 07/05/2017.
 */
public class ImageElementTestbench extends Application{
    private ImageElement elementUnderTest;
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
        try {
            ImageElement imageElement = new ImageElement();
            imageElement.setLayer(1);
            imageElement.setVisibility(true);
            imageElement.setStartSequence(1);
            imageElement.setEndSequence(2);
            imageElement.setPath("projectResources/logos/ediLogo400x400.png");
            imageElement.aspectRatioLock(false);

            imageElement.setPosX(0.7f);
            imageElement.setPosY(0f);
            imageElement.setHeight(0.5f);
            imageElement.setWidth(0.5f);
            imageElement.setRotation(45);
            imageElement.setBorder(true);
            imageElement.setBorderWidth(0.05f);
            imageElement.setBorderColour("#000000FF");

            ImageElement httpImageElement = new ImageElement();
            httpImageElement.setLayer(1);
            httpImageElement.setStartSequence(1);
            httpImageElement.setEndSequence(2);
            httpImageElement.setPath("https://www.google.co.uk/images/branding/googlelogo/2x/googlelogo_color_272x92dp.png");
            httpImageElement.aspectRatioLock(true);
            httpImageElement.setVisibility(true);

            httpImageElement.setPosX(0.1f);
            httpImageElement.setPosY(0.1f);
            httpImageElement.setWidth(0.4f);

            httpImageElement.setSlideWidth(scene.getWidth());
            httpImageElement.setSlideHeight(scene.getHeight());
            imageElement.setSlideWidth(scene.getWidth());
            imageElement.setSlideHeight(scene.getHeight());

            //Do the functions which would normally be done by a presentation manager
            httpImageElement.setSlideCanvas(root);
            imageElement.setSlideCanvas(root);
            httpImageElement.renderElement(Animation.ENTRY_ANIMATION);
            imageElement.renderElement(Animation.ENTRY_ANIMATION);
        } catch (FileNotFoundException fnfe){
            fnfe.printStackTrace();
        }

        primaryStage.setTitle("Image Element Test");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
