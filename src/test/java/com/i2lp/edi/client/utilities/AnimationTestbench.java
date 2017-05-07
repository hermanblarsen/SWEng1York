package com.i2lp.edi.client.utilities;

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
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

/**
 * Created by zain on 15/04/2017.
 *
 * See tests report for extected results.
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
//        TeacherPresentationController presentationController = new TeacherPresentationController();
//        presentationController.openPresentation("file:projectResources/sampleFiles/xml/sampleXmlSimpleAnimation.xml");
//        SVGPath testPath = new SVGPath();
//        testPath.setContent("M10 10 L100 100");
//        testPath.setStroke(Color.BLUE);
//        presentationController.border.getChildren().add(testPath);
//        testPath.setVisible(true);
//
//
//    }

    // This test is for manually verifying that the various animations work as expected as a unit.
    @Override
    public void start(Stage primaryStage) {
        // Setup the animations to test
        TranslationAnimation translationAnimation = new TranslationAnimation(100,0,500,500, 1000);
        ScaleAnimation scaleAnimation = new ScaleAnimation(0, 1, 1000);
        OpacityAnimation opacityAnimation = new OpacityAnimation( 0, 1, 1000);
        PathAnimation pathAnimation = new PathAnimation("M100 100, s-500 1200 500 800, c-500 -10 170 -800 170 -800 Z", 3000);

        // Setup test node
        Rectangle testRect = new Rectangle(50, 50);
        testRect.setTranslateX(500);
        testRect.setTranslateY(500);

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

        //Setup the root border pane, controls on the left.
        BorderPane root = new BorderPane();
        root.setLeft(buttonsBox);
        root.getChildren().add(testRect);

        Scene scene = new Scene(root, 1000, 1000);

        primaryStage.setTitle("Simple Animations Test");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
