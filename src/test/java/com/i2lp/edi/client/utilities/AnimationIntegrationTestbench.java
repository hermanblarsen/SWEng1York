package com.i2lp.edi.client.utilities;

import com.i2lp.edi.client.managers.PresentationManagerTeacher;
import javafx.application.Application;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

/**
 * Created by zain on 22/04/2017.
 */
public class AnimationIntegrationTestbench extends Application {

	public static void main(String[] args){
		launch(args);
	}


    @Override
    public void start(Stage primaryStage){
        PresentationManagerTeacher presentationManager = new PresentationManagerTeacher();
        presentationManager.openPresentation("file:projectResources/sampleFiles/xml/i2lpSampleXml.xml", false);
    }
}
