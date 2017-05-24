package com.i2lp.edi.client.presentationElements;

import com.i2lp.edi.client.managers.PresentationManagerTeacher;
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
        PresentationManagerTeacher presentationManager = new PresentationManagerTeacher();
        presentationManager.openPresentation("file:projectResources/sampleFiles/xmlTests/audioElementTest.xml",false);
    }
}

