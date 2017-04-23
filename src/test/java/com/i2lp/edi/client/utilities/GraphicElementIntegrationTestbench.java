package com.i2lp.edi.client.utilities;

import com.i2lp.edi.client.presentationViewer.TeacherPresentationManager;
import javafx.application.Application;
import javafx.stage.Stage;


/**
 * Created by zain on 22/04/2017.
 * Initialises a presentation for verifying that Graphic Elements work correctly.
 */
public class GraphicElementIntegrationTestbench extends Application{
		public static void main(String[] args){
		launch(args);
	}


	@Override
    public void start(Stage primaryStage){
        TeacherPresentationManager presentationManager = new TeacherPresentationManager();
        presentationManager.openPresentation("file:projectResources/sampleFiles/graphicElementTest.xml");
    }
}

