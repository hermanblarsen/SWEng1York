package com.i2lp.edi.client.presentationElements;

import com.i2lp.edi.client.presentationViewer.TeacherPresentationController;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Created by zain
 * Initialises a presentation for verifying that Audio Elements work correctly.
 */
public class AudioElementIntegrationTestbench extends Application {
    public static void main(String[] args){
        launch(args);
    }


    @Override
    public void start(Stage primaryStage){
        TeacherPresentationController presentationManager = new TeacherPresentationController();
        presentationManager.openPresentation("file:projectResources/sampleFiles/xmlTests/audioElementTest.xml");
    }
}

