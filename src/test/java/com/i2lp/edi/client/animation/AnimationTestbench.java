package com.i2lp.edi.client.animation;

import com.i2lp.edi.client.animation.OpacityAnimation;
import com.i2lp.edi.client.animation.PathAnimation;
import com.i2lp.edi.client.animation.ScaleAnimation;
import com.i2lp.edi.client.animation.TranslationAnimation;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;

/**
 * Created by zain on 15/04/2017.
 *
 * See tests report for expected results.
 */
public class AnimationTestbench extends Application {
    BorderPane mainPane;
    Scene mainScene;

    public static void main(String[] args){
        launch(args);
    }

//
//    @Override
//    public void start(Stage dashboardStage){
//        PresentationManagerTeacher presentationManager = new PresentationManagerTeacher();
//        presentationManager.openPresentation("file:projectResources/sampleFiles/xml/sampleXmlSimpleAnimation.xml");
//        SVGPath testPath = new SVGPath();
//        testPath.setContent("M-0.1 -0.1 L0.1 0.1 L0.1 0.9 L0.9 0.9 L0.2 0.2");
//        testPath.setStroke(Color.BLUE);
//        presentationManager.border.getChildren().add(testPath);
//        testPath.setVisible(true);
//
//
//    }

    // This test is for manually verifying that the various animations work as expected as a unit.
    @Override
    public void start(Stage primaryStage) {
        // Setup the animations to test
        TranslationAnimation translationAnimation = new TranslationAnimation(100,100,300,300, 1000);
        ScaleAnimation scaleAnimation = new ScaleAnimation(0, 1, 1000);
        OpacityAnimation opacityAnimation = new OpacityAnimation( 0, 1, 1000);
        PathAnimation pathAnimation = new PathAnimation("M100 100, s-500 1200 500 800, c-500 -10 170 -800 170 -800 Z", 3000);

        // Setup test node
        Rectangle testRect = new Rectangle(50, 50);
        testRect.setTranslateX(300);
        testRect.setTranslateY(300);

        // Bind the animations to the test node
        translationAnimation.setCoreNodeToAnimate(testRect);
        scaleAnimation.setCoreNodeToAnimate(testRect);
        opacityAnimation.setCoreNodeToAnimate(testRect);
        pathAnimation.setCoreNodeToAnimate(testRect);

        // Add Test Translation animation Button
        Button translationButton = new Button("Play Translation animation");
        translationButton.addEventHandler(MouseEvent.MOUSE_CLICKED, e-> translationAnimation.play());

        // Add Test Scale animation Button
        Button scaleButton = new Button("Play Scale animation");
        scaleButton.addEventHandler(MouseEvent.MOUSE_CLICKED, e-> scaleAnimation.play());

        // Add Test Opacity animation Button
        Button opacityButton = new Button("Play Opacity animation");
        opacityButton.addEventHandler(MouseEvent.MOUSE_CLICKED, e-> opacityAnimation.play());

        // Add Test Path animation Button
        Button pathButton = new Button("Play Path animation");
        pathButton.addEventHandler(MouseEvent.MOUSE_CLICKED, e-> pathAnimation.play());


        // Setup controls panel to window
        VBox buttonsBox = new VBox();
        buttonsBox.getChildren().add(translationButton);
        buttonsBox.getChildren().add(scaleButton);
        buttonsBox.getChildren().add(opacityButton);
        buttonsBox.getChildren().add(pathButton);

//        SVGPath testPath = new SVGPath();
//        testPath.setContent("M0 0 L0.1 0.1 L0.1 0.9 L0.9 0.9 L0.2 0.2");
//        //Scale scale = new Scale(1000, 500, 0,0);
//        //testPath.getTransforms().add(scale);
//        testPath.setScaleX(1);
//        testPath.setStroke(Color.BLUE);
//        testPath.setStrokeWidth(0.01);
//        testPath.setFill(Color.TRANSPARENT);

        //Setup the root border pane, controls on the left.
        BorderPane root = new BorderPane();
        root.setLeft(buttonsBox);
        root.getChildren().add(testRect);
//        root.getChildren().add(testPath);

        Scene scene = new Scene(root, 600, 600);
        pathAnimation.setScaleFactor(1,1);
        translationAnimation.setScaleFactor(1,1);

        primaryStage.setTitle("Simple Animations Test");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
